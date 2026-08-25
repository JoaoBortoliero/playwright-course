package br.com.curso.playwright.solutions;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Laboratório independente com os mesmos contratos usados pelo projeto do aluno. */
final class ReferenceLab implements AutoCloseable {
  private final HttpServer server;
  private final ExecutorService executor;

  private ReferenceLab(HttpServer server, ExecutorService executor) {
    this.server = server;
    this.executor = executor;
  }

  static ReferenceLab start() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    ExecutorService executor = Executors.newCachedThreadPool();
    ReferenceLab lab = new ReferenceLab(server, executor);
    lab.routes();
    server.setExecutor(executor);
    server.start();
    return lab;
  }

  String url() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  @Override
  public void close() {
    server.stop(0);
    executor.shutdownNow();
  }

  private void routes() {
    server.createContext("/", exchange -> html(exchange, home()));
    server.createContext("/interactions", exchange -> html(exchange, interactions()));
    server.createContext("/frame", exchange -> html(exchange,
        "<main><h1>Área incorporada</h1><button onclick=\"this.textContent='Concluído'\">Confirmar</button></main>"));
    server.createContext("/popup", exchange -> html(exchange,
        "<main><h1>Nova janela</h1><p data-test='popup-status'>Popup carregado</p></main>"));
    server.createContext("/download/report.txt", exchange -> respond(exchange, 200,
        "text/plain", "relatório determinístico\n"));
    server.createContext("/network", exchange -> html(exchange, network()));
    server.createContext("/auth", exchange -> html(exchange, auth()));
    server.createContext("/api/items", exchange -> {
      if ("POST".equals(exchange.getRequestMethod())) {
        respond(exchange, 201, "application/json", "{\"id\":3,\"name\":\"Novo item\"}");
      } else {
        respond(exchange, 200, "application/json",
            "[{\"id\":1,\"name\":\"Backpack\"},{\"id\":2,\"name\":\"Bike Light\"}]");
      }
    });
    server.createContext("/api/profile", exchange -> {
      String cookie = exchange.getRequestHeaders().getFirst("Cookie");
      if (cookie != null && cookie.contains("session=student")) {
        respond(exchange, 200, "application/json", "{\"user\":\"student\",\"role\":\"qa\"}");
      } else {
        respond(exchange, 401, "application/json", "{\"error\":\"unauthorized\"}");
      }
    });
    server.createContext("/api/error", exchange ->
        respond(exchange, 503, "application/json", "{\"error\":\"maintenance\"}"));
    server.createContext("/api/blocked", exchange ->
        respond(exchange, 200, "application/json", "{\"status\":\"should-be-aborted\"}"));
  }

  private static String shell(String body) {
    return "<!doctype html><html lang='pt-BR'><head><meta charset='utf-8'><title>Playwright Lab</title>"
        + "<style>body{font-family:sans-serif}iframe{width:100%;height:150px}</style></head><body>"
        + body + "</body></html>";
  }

  private static String home() {
    return shell("<main><h1>Playwright Lab</h1><nav aria-label='Laboratórios'>"
        + "<a href='/interactions'>Interações</a><a href='/network'>Rede</a><a href='/auth'>Autenticação</a>"
        + "</nav><p data-test='ready'>ready</p></main>");
  }

  private static String interactions() {
    return shell("""
        <main><h1>Interações avançadas</h1>
        <label>Arquivo <input type='file' aria-label='Arquivo'></label><p data-test='file-name'></p>
        <a href='/download/report.txt' download>Baixar relatório</a>
        <button onclick="alert('Confirmação do laboratório')">Abrir diálogo</button>
        <button onclick="window.open('/popup','lab-popup')">Abrir popup</button>
        <label>Atalho <input aria-label='Atalho' onkeydown="if(event.key==='Enter')document.querySelector('[data-test=key]').textContent='Enter recebido'"></label>
        <p data-test='key'></p><iframe title='Área incorporada' src='/frame'></iframe>
        <button onclick="setTimeout(()=>{document.querySelector('[data-test=delayed]').hidden=false},100)">Carregar</button>
        <p data-test='delayed' hidden>Conteúdo pronto</p></main>
        <script>document.querySelector('input[type=file]').onchange=e=>document.querySelector('[data-test=file-name]').textContent=e.target.files[0].name</script>
        """);
  }

  private static String network() {
    return shell("""
        <main><h1>Laboratório de rede</h1>
        <button onclick="load('/api/items','items')">Carregar itens</button>
        <button onclick="load('/api/blocked','blocked')">Requisição bloqueável</button>
        <pre data-test='result'></pre></main>
        <script>async function load(url,kind){try{let r=await fetch(url);let b=await r.json();document.querySelector('[data-test=result]').textContent=kind+':'+JSON.stringify(b)}catch(e){document.querySelector('[data-test=result]').textContent=kind+':erro'}}</script>
        """);
  }

  private static String auth() {
    return shell("""
        <main><h1>Autenticação local</h1><button onclick="login()">Entrar</button>
        <p data-test='session'></p></main><script>
        function login(){document.cookie='session=student; path=/';localStorage.setItem('role','qa');document.querySelector('[data-test=session]').textContent='Autenticado'}</script>
        """);
  }

  private static void html(HttpExchange exchange, String body) throws IOException {
    respond(exchange, 200, "text/html; charset=utf-8", body);
  }

  private static void respond(HttpExchange exchange, int status, String type, String body)
      throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", type);
    exchange.getResponseHeaders().set("Cache-Control", "no-store");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }
}
