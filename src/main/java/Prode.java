import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Prode {

    // ╔══════════════════════════════════════════════════════════╗
    // ║  BST GENÉRICO                                           ║
    // ╚══════════════════════════════════════════════════════════╝
    static class BST<T extends Comparable<T>> {
        static class Nodo<T> {
            T dato; Nodo<T> izq, der;
            Nodo(T d) { dato = d; }
        }
        Nodo<T> raiz;

        boolean estaVacio() { return raiz == null; }

        void insertar(T d) { raiz = ins(raiz, d); }
        private Nodo<T> ins(Nodo<T> n, T d) {
            if (n == null) return new Nodo<>(d);
            int c = d.compareTo(n.dato);
            if (c < 0) n.izq = ins(n.izq, d);
            else if (c > 0) n.der = ins(n.der, d);
            return n;
        }

        boolean buscar(T d) { return bsc(raiz, d) != null; }
        private Nodo<T> bsc(Nodo<T> n, T d) {
            if (n == null) return null;
            int c = d.compareTo(n.dato);
            return c == 0 ? n : c < 0 ? bsc(n.izq, d) : bsc(n.der, d);
        }

        int altura() { return h(raiz); }
        private int h(Nodo<T> n) { return n == null ? 0 : 1 + Math.max(h(n.izq), h(n.der)); }

        String toJson() { return nj(raiz); }
        private String nj(Nodo<T> n) {
            if (n == null) return "null";
            StringBuilder s = new StringBuilder("{\"name\":\"").append(esc(n.dato.toString())).append("\"");
            List<String> ch = new ArrayList<>();
            if (n.izq != null) ch.add(nj(n.izq));
            if (n.der != null) ch.add(nj(n.der));
            if (!ch.isEmpty()) s.append(",\"children\":[").append(String.join(",", ch)).append("]");
            return s.append("}").toString();
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  AVL GENÉRICO                                           ║
    // ╚══════════════════════════════════════════════════════════╝
    static class AVL<T extends Comparable<T>> {
        static class Nodo<T> {
            T dato; Nodo<T> izq, der; int h;
            Nodo(T d) { dato = d; h = 1; }
        }
        Nodo<T> raiz;

        boolean estaVacio() { return raiz == null; }
        private int h(Nodo<T> n) { return n == null ? 0 : n.h; }
        private int fb(Nodo<T> n) { return n == null ? 0 : h(n.izq) - h(n.der); }
        private void uh(Nodo<T> n) { if (n != null) n.h = 1 + Math.max(h(n.izq), h(n.der)); }

        private Nodo<T> rd(Nodo<T> y) {
            Nodo<T> x = y.izq, t = x.der;
            x.der = y; y.izq = t; uh(y); uh(x); return x;
        }
        private Nodo<T> ri(Nodo<T> x) {
            Nodo<T> y = x.der, t = y.izq;
            y.izq = x; x.der = t; uh(x); uh(y); return y;
        }

        void insertar(T d) { raiz = ins(raiz, d); }
        private Nodo<T> ins(Nodo<T> n, T d) {
            if (n == null) return new Nodo<>(d);
            int c = d.compareTo(n.dato);
            if (c < 0) n.izq = ins(n.izq, d);
            else if (c > 0) n.der = ins(n.der, d);
            else return n;
            uh(n);
            int b = fb(n);
            if (b > 1  && d.compareTo(n.izq.dato) < 0) return rd(n);
            if (b < -1 && d.compareTo(n.der.dato) > 0) return ri(n);
            if (b > 1  && d.compareTo(n.izq.dato) > 0) { n.izq = ri(n.izq); return rd(n); }
            if (b < -1 && d.compareTo(n.der.dato) < 0) { n.der = rd(n.der); return ri(n); }
            return n;
        }

        void eliminar(T d) { raiz = del(raiz, d); }
        private Nodo<T> del(Nodo<T> n, T d) {
            if (n == null) return null;
            int c = d.compareTo(n.dato);
            if (c < 0) n.izq = del(n.izq, d);
            else if (c > 0) n.der = del(n.der, d);
            else {
                if (n.izq == null || n.der == null) return n.izq == null ? n.der : n.izq;
                Nodo<T> m = n.der; while (m.izq != null) m = m.izq;
                n.dato = m.dato; n.der = del(n.der, m.dato);
            }
            uh(n); int b = fb(n);
            if (b > 1  && fb(n.izq) >= 0) return rd(n);
            if (b > 1)  { n.izq = ri(n.izq); return rd(n); }
            if (b < -1 && fb(n.der) <= 0) return ri(n);
            if (b < -1) { n.der = rd(n.der); return ri(n); }
            return n;
        }

        boolean buscar(T d) { return bsc(raiz, d) != null; }
        Nodo<T> bsc(Nodo<T> n, T d) {
            if (n == null) return null;
            int c = d.compareTo(n.dato);
            return c == 0 ? n : c < 0 ? bsc(n.izq, d) : bsc(n.der, d);
        }

        int altura() { return h(raiz); }

        String toJson() { return nj(raiz); }
        private String nj(Nodo<T> n) {
            if (n == null) return "null";
            StringBuilder s = new StringBuilder("{\"name\":\"").append(esc(n.dato.toString())).append("\"")
                    .append(",\"h\":").append(n.h);
            if (n.dato instanceof PK pk) s.append(",\"pts\":").append(pk.p);
            else {
                s.append(",\"fb\":").append(fb(n));
                if (n.dato instanceof String nombre) {
                    Estudiante e = estudiantes.get(nombre);
                    if (e != null) s.append(",\"pts\":").append(e.puntaje);
                }
            }
            List<String> ch = new ArrayList<>();
            if (n.izq != null) ch.add(nj(n.izq));
            if (n.der != null) ch.add(nj(n.der));
            if (!ch.isEmpty()) s.append(",\"children\":[").append(String.join(",", ch)).append("]");
            return s.append("}").toString();
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  DOMINIO                                                ║
    // ╚══════════════════════════════════════════════════════════╝
    static class PK implements Comparable<PK> {
        int p; List<String> nombres = new ArrayList<>();
        PK(int p) { this.p = p; }
        PK(int p, String n) { this.p = p; nombres.add(n); }
        public int compareTo(PK o) { return Integer.compare(p, o.p); }
        public String toString() {
            return nombres.isEmpty() ? p + "pts" : p + "pts(" + nombres.size() + ")";
        }
    }

    static class Partido {
        int id; String eq1, eq2, res, fase, grupo;
        Partido(int id, String e1, String e2, String fase, String grupo) {
            this.id = id; eq1 = e1; eq2 = e2; this.fase = fase; this.grupo = grupo;
        }
    }

    static class Estudiante {
        String nombre; int puntaje = 0;
        Map<String, String[]> pronGrupos = new LinkedHashMap<>();
        Map<Integer, String> pronPartidos = new HashMap<>();
        Estudiante(String n) { nombre = n; }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  ESTADO GLOBAL                                          ║
    // ╚══════════════════════════════════════════════════════════╝
    static final Map<String, List<String>> GRUPOS = new LinkedHashMap<>();
    static final Map<String, String[]>     resGrupos  = new LinkedHashMap<>();
    static final List<Partido>             partidos   = new ArrayList<>();
    static final Map<String, Estudiante>   estudiantes = new LinkedHashMap<>();
    static BST<String>  bstNombres  = new BST<>();
    static AVL<String>  avlNombres  = new AVL<>();
    static AVL<PK>      avlPuntajes = new AVL<>();
    static int nextId = 1;
    static final String SAVE = "prode_data.txt";
    static final String PASS = "profe2026";

    // ╔══════════════════════════════════════════════════════════╗
    // ║  INICIALIZACIÓN                                         ║
    // ╚══════════════════════════════════════════════════════════╝
    static void initGrupos() {
        String[][] data = {
            {"A","Mexico","South Korea","South Africa","Czechia"},
            {"B","Canada","Switzerland","Qatar","Bosnia-Herzegovina"},
            {"C","Brazil","Morocco","Haiti","Scotland"},
            {"D","USA","Paraguay","Australia","Turkey"},
            {"E","Germany","Ivory Coast","Ecuador","Curazao"},
            {"F","Netherlands","Japan","Sweden","Tunisia"},
            {"G","Belgium","Egypt","Iran","New Zealand"},
            {"H","Spain","Uruguay","Saudi Arabia","Cape Verde"},
            {"I","France","Senegal","Norway","Iraq"},
            {"J","Argentina","Austria","Algeria","Jordan"},
            {"K","Portugal","Colombia","DR Congo","Uzbekistan"},
            {"L","England","Croatia","Panama","Ghana"}
        };
        for (String[] row : data)
            GRUPOS.put(row[0], new ArrayList<>(Arrays.asList(row[1], row[2], row[3], row[4])));
    }

    static void initBracket() {
        String[] fases = new String[30];
        Arrays.fill(fases,  0, 16, "OCTAVOS");
        Arrays.fill(fases, 16, 24, "CUARTOS");
        Arrays.fill(fases, 24, 28, "SEMIS");
        fases[28] = "TERCER_PUESTO"; fases[29] = "FINAL";
        for (String f : fases) partidos.add(new Partido(nextId++, "TBD", "TBD", f, null));
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  MAIN                                                   ║
    // ╚══════════════════════════════════════════════════════════╝
    public static void main(String[] args) throws IOException {
        initGrupos();
        initBracket();
        cargar();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        server.createContext("/",                  ex -> wrap(ex, "GET",  Prode::root));
        server.createContext("/api/state",         ex -> wrap(ex, "GET",  Prode::apiState));
        server.createContext("/api/trees",         ex -> wrap(ex, "GET",  Prode::apiTrees));
        server.createContext("/api/inscribir",     ex -> wrap(ex, "POST", Prode::apiInscribir));
        server.createContext("/api/pron_grupo",    ex -> wrap(ex, "POST", Prode::apiPronGrupo));
        server.createContext("/api/pron_partido",  ex -> wrap(ex, "POST", Prode::apiPronPartido));
        server.createContext("/api/res_grupo",     ex -> wrap(ex, "POST", Prode::apiResGrupo));
        server.createContext("/api/set_partido",   ex -> wrap(ex, "POST", Prode::apiSetPartido));
        server.createContext("/api/res_partido",   ex -> wrap(ex, "POST", Prode::apiResPartido));
        server.createContext("/api/reiniciar",     ex -> wrap(ex, "POST", Prode::apiReiniciar));

        server.start();
        System.out.println("Servidor iniciado en puerto " + port);
    }

    @FunctionalInterface interface H { void run(HttpExchange ex) throws IOException; }

    static void wrap(HttpExchange ex, String method, H h) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        if ("OPTIONS".equals(ex.getRequestMethod())) { respond(ex, 200, ""); return; }
        if (!method.equals(ex.getRequestMethod())) { respond(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }
        try { h.run(ex); }
        catch (Exception e) {
            e.printStackTrace();
            try { respond(ex, 500, "{\"error\":\"" + esc(e.getMessage()) + "\"}"); } catch (Exception ignored) {}
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  HANDLERS HTTP                                          ║
    // ╚══════════════════════════════════════════════════════════╝
    static void root(HttpExchange ex) throws IOException {
        respondHtml(ex, HTML);
    }

    static synchronized void apiState(HttpExchange ex) throws IOException {
        StringBuilder sb = new StringBuilder("{\"grupos\":{");
        boolean fg = true;
        for (Map.Entry<String, List<String>> e : GRUPOS.entrySet()) {
            if (!fg) sb.append(","); fg = false;
            sb.append("\"").append(e.getKey()).append("\":[");
            List<String> eq = e.getValue();
            for (int i = 0; i < eq.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(esc(eq.get(i))).append("\"");
            }
            sb.append("]");
        }
        sb.append("},\"resGrupos\":{");
        boolean fr = true;
        for (Map.Entry<String, String[]> e : resGrupos.entrySet()) {
            if (!fr) sb.append(","); fr = false;
            sb.append("\"").append(e.getKey()).append("\":[");
            String[] v = e.getValue();
            for (int i = 0; i < v.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(esc(v[i])).append("\"");
            }
            sb.append("]");
        }
        sb.append("},\"partidos\":[");
        for (int i = 0; i < partidos.size(); i++) {
            Partido p = partidos.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":").append(p.id)
              .append(",\"eq1\":\"").append(esc(p.eq1))
              .append("\",\"eq2\":\"").append(esc(p.eq2))
              .append("\",\"fase\":\"").append(p.fase)
              .append("\",\"res\":").append(p.res == null ? "null" : "\"" + p.res + "\"")
              .append("}");
        }
        sb.append("],\"ranking\":[");
        List<Estudiante> ranked = new ArrayList<>(estudiantes.values());
        ranked.sort((a, b) -> b.puntaje - a.puntaje);
        for (int i = 0; i < ranked.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(esc(ranked.get(i).nombre))
              .append("\",\"puntaje\":").append(ranked.get(i).puntaje).append("}");
        }
        sb.append("]}");
        respondJson(ex, sb.toString());
    }

    static synchronized void apiTrees(HttpExchange ex) throws IOException {
        String bst  = bstNombres.estaVacio()  ? "null" : bstNombres.toJson();
        String avl  = avlNombres.estaVacio()  ? "null" : avlNombres.toJson();
        String avlp = avlPuntajes.estaVacio() ? "null" : avlPuntajes.toJson();
        respondJson(ex, "{\"bst\":" + bst + ",\"avl\":" + avl + ",\"avlPuntajes\":" + avlp
                + ",\"alturaBST\":" + bstNombres.altura()
                + ",\"alturaAVL\":" + avlNombres.altura() + "}");
    }

    static synchronized void apiInscribir(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        String nombre = b.get("nombre");
        if (nombre == null || nombre.isBlank()) { respondJson(ex, "{\"error\":\"Nombre requerido\"}"); return; }
        nombre = nombre.trim();
        if (!estudiantes.containsKey(nombre)) {
            estudiantes.put(nombre, new Estudiante(nombre));
            bstNombres.insertar(nombre);
            avlNombres.insertar(nombre);
            guardar();
        }
        respondJson(ex, "{\"ok\":true,\"nombre\":\"" + esc(nombre) + "\"}");
    }

    static synchronized void apiPronGrupo(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        String nombre  = b.get("nombre");
        String grupo   = b.get("grupo");
        String primero = b.get("primero");
        String segundo = b.get("segundo");
        if (nombre == null || grupo == null || primero == null || segundo == null) {
            respondJson(ex, "{\"error\":\"Datos incompletos\"}"); return;
        }
        Estudiante e = estudiantes.get(nombre.trim());
        if (e == null) { respondJson(ex, "{\"error\":\"Estudiante no encontrado\"}"); return; }
        e.pronGrupos.put(grupo, new String[]{primero, segundo});
        recalcPuntajes(); guardar();
        respondJson(ex, "{\"ok\":true}");
    }

    static synchronized void apiPronPartido(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        String nombre = b.get("nombre");
        String idStr  = b.get("id");
        String pred   = b.get("pred");
        if (nombre == null || idStr == null || pred == null) {
            respondJson(ex, "{\"error\":\"Datos incompletos\"}"); return;
        }
        Estudiante e = estudiantes.get(nombre.trim());
        if (e == null) { respondJson(ex, "{\"error\":\"Estudiante no encontrado\"}"); return; }
        int id = Integer.parseInt(idStr.trim());
        Partido p = partidos.stream().filter(pt -> pt.id == id).findFirst().orElse(null);
        if (p == null || p.res != null) { respondJson(ex, "{\"error\":\"Partido no valido o ya jugado\"}"); return; }
        e.pronPartidos.put(id, pred);
        recalcPuntajes(); guardar();
        respondJson(ex, "{\"ok\":true}");
    }

    static synchronized void apiResGrupo(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        if (!PASS.equals(b.get("pass"))) { respondJson(ex, "{\"error\":\"Contrasena incorrecta\"}"); return; }
        String grupo = b.get("grupo");
        String p1 = b.get("p1"), p2 = b.get("p2");
        String p3 = b.getOrDefault("p3", ""), p4 = b.getOrDefault("p4", "");
        if (grupo == null || p1 == null || p2 == null) { respondJson(ex, "{\"error\":\"Datos incompletos\"}"); return; }
        resGrupos.put(grupo, new String[]{p1, p2, p3, p4});
        recalcPuntajes(); guardar();
        respondJson(ex, "{\"ok\":true}");
    }

    static synchronized void apiSetPartido(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        if (!PASS.equals(b.get("pass"))) { respondJson(ex, "{\"error\":\"Contrasena incorrecta\"}"); return; }
        int id = Integer.parseInt(b.getOrDefault("id", "0").trim());
        Partido p = partidos.stream().filter(pt -> pt.id == id).findFirst().orElse(null);
        if (p == null) { respondJson(ex, "{\"error\":\"Partido no encontrado\"}"); return; }
        if (b.containsKey("eq1")) p.eq1 = b.get("eq1");
        if (b.containsKey("eq2")) p.eq2 = b.get("eq2");
        guardar();
        respondJson(ex, "{\"ok\":true}");
    }

    static synchronized void apiResPartido(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        if (!PASS.equals(b.get("pass"))) { respondJson(ex, "{\"error\":\"Contrasena incorrecta\"}"); return; }
        int id  = Integer.parseInt(b.getOrDefault("id", "0").trim());
        String res = b.get("res");
        Partido p = partidos.stream().filter(pt -> pt.id == id).findFirst().orElse(null);
        if (p == null) { respondJson(ex, "{\"error\":\"Partido no encontrado\"}"); return; }
        p.res = res;
        recalcPuntajes(); guardar();
        respondJson(ex, "{\"ok\":true}");
    }

    static synchronized void apiReiniciar(HttpExchange ex) throws IOException {
        Map<String, String> b = parseJson(body(ex));
        if (!PASS.equals(b.get("pass"))) { respondJson(ex, "{\"error\":\"Contrasena incorrecta\"}"); return; }
        reiniciarJuego();
        respondJson(ex, "{\"ok\":true}");
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  LÓGICA DE NEGOCIO                                      ║
    // ╚══════════════════════════════════════════════════════════╝
    static void reiniciarJuego() throws IOException {
        estudiantes.clear();
        resGrupos.clear();
        bstNombres = new BST<>();
        avlNombres = new AVL<>();
        avlPuntajes = new AVL<>();
        partidos.clear();
        nextId = 1;
        initBracket();
        guardar();
    }

    static void recalcPuntajes() {
        avlPuntajes = new AVL<>();
        for (Estudiante e : estudiantes.values()) {
            e.puntaje = 0;
            for (Map.Entry<String, String[]> pg : e.pronGrupos.entrySet()) {
                String[] pred = pg.getValue(), real = resGrupos.get(pg.getKey());
                if (real == null || real.length < 2) continue;
                if (pred[0] != null && pred[0].equals(real[0]))                  e.puntaje += 3;
                else if (pred[0] != null && pred[0].equals(real[1]))             e.puntaje += 1;
                if (pred.length > 1 && pred[1] != null && pred[1].equals(real[1])) e.puntaje += 2;
                else if (pred.length > 1 && pred[1] != null && pred[1].equals(real[0])) e.puntaje += 1;
            }
            for (Partido p : partidos) {
                if (p.res == null) continue;
                if (p.res.equals(e.pronPartidos.get(p.id))) e.puntaje += 2;
            }
            AVL.Nodo<PK> nodo = avlPuntajes.bsc(avlPuntajes.raiz, new PK(e.puntaje));
            if (nodo != null) nodo.dato.nombres.add(e.nombre);
            else avlPuntajes.insertar(new PK(e.puntaje, e.nombre));
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  PERSISTENCIA (formato línea)                           ║
    // ╚══════════════════════════════════════════════════════════╝
    static void guardar() {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Path.of(SAVE), StandardCharsets.UTF_8))) {
            for (Estudiante e : estudiantes.values()) {
                pw.println("EST|" + e.nombre);
                for (Map.Entry<String, String[]> pg : e.pronGrupos.entrySet())
                    pw.println("PG|" + e.nombre + "|" + pg.getKey() + "|" + pg.getValue()[0]
                            + "|" + (pg.getValue().length > 1 ? pg.getValue()[1] : ""));
                for (Map.Entry<Integer, String> pp : e.pronPartidos.entrySet())
                    pw.println("PP|" + e.nombre + "|" + pp.getKey() + "|" + pp.getValue());
            }
            for (Map.Entry<String, String[]> rg : resGrupos.entrySet())
                pw.println("RG|" + rg.getKey() + "|" + String.join("|", rg.getValue()));
            for (Partido p : partidos)
                pw.println("PTD|" + p.id + "|" + p.fase + "|" + p.eq1 + "|" + p.eq2
                        + "|" + (p.res == null ? "" : p.res));
        } catch (IOException e) {
            System.err.println("Error guardando: " + e.getMessage());
        }
    }

    static void cargar() {
        if (!Files.exists(Path.of(SAVE))) return;
        try {
            for (String line : Files.readAllLines(Path.of(SAVE), StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);
                if (p.length == 0) continue;
                switch (p[0]) {
                    case "EST" -> {
                        if (p.length > 1 && !p[1].isBlank()) {
                            estudiantes.put(p[1], new Estudiante(p[1]));
                            bstNombres.insertar(p[1]);
                            avlNombres.insertar(p[1]);
                        }
                    }
                    case "PG" -> {
                        if (p.length >= 5) {
                            Estudiante e = estudiantes.get(p[1]);
                            if (e != null) e.pronGrupos.put(p[2], new String[]{p[3], p[4]});
                        }
                    }
                    case "PP" -> {
                        if (p.length >= 4) {
                            Estudiante e = estudiantes.get(p[1]);
                            if (e != null) e.pronPartidos.put(Integer.parseInt(p[2]), p[3]);
                        }
                    }
                    case "RG" -> {
                        if (p.length >= 3) resGrupos.put(p[1], Arrays.copyOfRange(p, 2, p.length));
                    }
                    case "PTD" -> {
                        if (p.length >= 6) {
                            int id = Integer.parseInt(p[1]);
                            partidos.stream().filter(x -> x.id == id).findFirst().ifPresent(pt -> {
                                pt.eq1 = p[3]; pt.eq2 = p[4];
                                pt.res = p[5].isBlank() ? null : p[5];
                            });
                        }
                    }
                }
            }
            recalcPuntajes();
        } catch (IOException e) {
            System.err.println("Error cargando: " + e.getMessage());
        }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  UTILS                                                  ║
    // ╚══════════════════════════════════════════════════════════╝
    static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1, json.endsWith("}") ? json.length() - 1 : json.length());
        int i = 0;
        while (i < json.length()) {
            while (i < json.length() && json.charAt(i) != '"') i++;
            if (i >= json.length()) break;
            int ks = i + 1, ke = json.indexOf('"', ks);
            if (ke < 0) break;
            String key = json.substring(ks, ke);
            i = ke + 1;
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            String val;
            if (i < json.length() && json.charAt(i) == '"') {
                int vs = i + 1, ve = vs;
                while (ve < json.length()) {
                    if (json.charAt(ve) == '\\') ve++;
                    else if (json.charAt(ve) == '"') break;
                    ve++;
                }
                val = json.substring(vs, ve).replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
                i = ve + 1;
            } else {
                int ve = i;
                while (ve < json.length() && json.charAt(ve) != ',' && json.charAt(ve) != '}') ve++;
                val = json.substring(i, ve).trim();
                i = ve;
            }
            map.put(key, val);
            while (i < json.length() && json.charAt(i) != '"' && json.charAt(i) != '}') i++;
        }
        return map;
    }

    static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
    static void respondJson(HttpExchange ex, String body) throws IOException { respond(ex, 200, body); }
    static void respondHtml(HttpExchange ex, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    // ╔══════════════════════════════════════════════════════════╗
    // ║  HTML EMBEBIDO                                          ║
    // ╚══════════════════════════════════════════════════════════╝
    static final String HTML = """
<!DOCTYPE html>
<html lang="es" data-bs-theme="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Prode Mundial 2026</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://d3js.org/d3.v7.min.js"></script>
<style>
body{background:#0d1117;color:#e6edf3;font-family:'Segoe UI',sans-serif}
.nav-tabs .nav-link{color:#8b949e;border-radius:8px 8px 0 0}
.nav-tabs .nav-link.active{background:#161b22;color:#58a6ff;border-color:#30363d #30363d #161b22}
.card{background:#161b22;border:1px solid #30363d;border-radius:10px}
.card-header{background:#1c2128;border-bottom:1px solid #30363d}
.grupo-card{transition:transform .2s}.grupo-card:hover{transform:translateY(-3px)}
.equipo-item{padding:6px 12px;border-radius:6px;margin:3px 0;background:#0d1117;font-size:.9rem}
.equipo-item.prim{background:#1a3a2a;border-left:3px solid #3fb950}
.equipo-item.seg{background:#2a1a1a;border-left:3px solid #f0883e}
.badge-g{background:#388bfd22;color:#58a6ff;border:1px solid #388bfd55;padding:3px 10px;border-radius:12px;font-size:.7rem;font-weight:700;letter-spacing:1px}
.bm{background:#1c2128;border:1px solid #30363d;border-radius:8px;padding:8px 12px;min-width:155px}
.bm .t{padding:4px 8px;border-radius:4px;font-size:.82rem}
.bm .t.win{background:#1a3a2a;color:#3fb950;font-weight:700}
.stat-box{background:#161b22;border:1px solid #30363d;border-radius:10px;padding:16px;text-align:center}
.stat-num{font-size:2.2rem;font-weight:700;color:#58a6ff}
.tree-wrap{overflow-x:auto;min-height:280px}
.step{display:none}.step.on{display:block}
</style>
</head>
<body>
<div class="container-fluid px-4 py-3">

  <div class="d-flex align-items-center gap-3 mb-4">
    <div style="font-size:2.4rem">&#9917;</div>
    <div>
      <h2 class="mb-0 fw-bold" style="color:#58a6ff">Prode Mundial 2026</h2>
      <small class="text-muted">USA &middot; Canada &middot; Mexico &nbsp;|&nbsp; Estructuras de datos: BST &amp; AVL</small>
    </div>
    <div class="ms-auto">
      <button class="btn btn-danger btn-sm" onclick="openAdmin()">&#128274; Admin</button>
    </div>
  </div>

  <ul class="nav nav-tabs mb-3" id="mainTab">
    <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#tGrupos">&#127942; Grupos</a></li>
    <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tBracket">&#128203; Bracket</a></li>
    <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tPron">&#127919; Pronósticos</a></li>
    <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tRanking">&#128202; Ranking</a></li>
    <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tArboles" id="tabArbolesLink">&#127795; Árboles</a></li>
  </ul>

  <div class="tab-content">

    <!-- GRUPOS -->
    <div class="tab-pane fade show active" id="tGrupos">
      <div class="row g-3" id="gruposGrid"></div>
    </div>

    <!-- BRACKET -->
    <div class="tab-pane fade" id="tBracket">
      <div class="overflow-auto pb-2" id="bracketWrap"></div>
    </div>

    <!-- PRONÓSTICOS -->
    <div class="tab-pane fade" id="tPron">
      <div class="row justify-content-center">
        <div class="col-lg-9">
          <div class="d-flex justify-content-end mb-2">
            <button type="button" class="btn btn-outline-warning btn-sm" onclick="datosAlAzar()">&#127922; Datos al azar</button>
          </div>
          <div id="s0" class="step on card p-4">
            <h5 class="fw-bold mb-3">&#128100; ¿Cómo te llamás?</h5>
            <div class="input-group input-group-lg">
              <input id="inputNombre" type="text" class="form-control" placeholder="Tu nombre completo" onkeydown="if(event.key==='Enter')inscribir()">
              <button class="btn btn-primary" onclick="inscribir()">Continuar &#8594;</button>
            </div>
            <div class="text-muted small mt-2">Si ya te inscribiste, ingresá el mismo nombre para editar pronósticos.</div>
          </div>
          <div id="s1" class="step">
            <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
              <h5 class="fw-bold mb-0">&#127942; Predecí 1° y 2° de cada grupo <span id="userLabel" class="badge bg-primary ms-2"></span></h5>
              <button type="button" class="btn btn-outline-warning btn-sm ms-auto" onclick="llenarGruposAlAzar()">&#127922; Grupos al azar</button>
            </div>
            <div class="row g-3" id="pronGruposForm"></div>
            <div class="d-flex flex-wrap gap-2 mt-3">
              <button class="btn btn-primary" onclick="goPartidos()">Siguiente: Bracket &#8594;</button>
              <button type="button" class="btn btn-outline-warning" onclick="datosAlAzar()">&#127922; Completar todo al azar</button>
            </div>
          </div>
          <div id="s2" class="step">
            <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
              <h5 class="fw-bold mb-0">&#9876;&#65039; Partidos de eliminación directa</h5>
              <button type="button" class="btn btn-outline-warning btn-sm ms-auto" onclick="llenarPartidosAlAzar()">&#127922; Partidos al azar</button>
            </div>
            <div id="pronPartidosForm"></div>
            <button class="btn btn-success mt-3" onclick="savePartidos()">&#128190; Guardar pronósticos</button>
          </div>
          <div id="s3" class="step card p-4 text-center">
            <div style="font-size:3rem">&#10004;&#65039;</div>
            <h4 class="fw-bold mt-2">¡Pronósticos guardados!</h4>
            <div class="text-muted">Volvé al ranking para ver tu posición.</div>
          </div>
        </div>
      </div>
    </div>

    <!-- RANKING -->
    <div class="tab-pane fade" id="tRanking">
      <div class="row g-3 mb-4" id="statsRow"></div>
      <div class="row g-3">
        <div class="col-lg-5">
          <div class="card">
            <div class="card-header fw-bold">&#127885; Tabla de posiciones</div>
            <table class="table table-dark table-hover mb-0">
              <thead><tr><th>#</th><th>Jugador</th><th class="text-end">Pts</th></tr></thead>
              <tbody id="rankingTbl"></tbody>
            </table>
          </div>
        </div>
        <div class="col-lg-7">
          <div class="card">
            <div class="card-header fw-bold">&#127795; AVL de Puntajes
              <small class="text-muted fw-normal ms-2">clave=puntos, valor=jugadores &nbsp;|&nbsp; <code>pts</code> = puntaje</small></div>
            <div class="card-body tree-wrap" id="avlPtsWrap"></div>
            <div class="card-footer border-secondary py-2 small text-muted">
              &#128992; Nodo resaltado = mayor puntaje &nbsp; <code>h</code> = altura del subárbol
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ÁRBOLES -->
    <div class="tab-pane fade" id="tArboles">
      <div class="alert mb-3" style="background:#1a3a2a;border:1px solid #3fb950;color:#aff5b4">
        <strong>&#128218; Visualización educativa:</strong>
        Los mismos nombres de estudiantes se insertan en ambos árboles en el mismo orden.
        El <strong>BST</strong> puede desbalancearse; el <strong>AVL</strong> se autobalancea con rotaciones.
        El factor de balance (<strong>fb</strong>) indica la diferencia de alturas izquierda-derecha.
      </div>
      <div class="row g-3 mb-3">
        <div class="col-6 col-md-3">
          <div class="stat-box"><div class="stat-num" id="hBST">-</div><div class="text-muted small">Altura BST</div></div>
        </div>
        <div class="col-6 col-md-3">
          <div class="stat-box"><div class="stat-num" style="color:#f0883e" id="hAVL">-</div><div class="text-muted small">Altura AVL</div></div>
        </div>
        <div class="col-md-6 d-flex align-items-center">
          <div class="small text-muted">
            &#128308; nodo con |fb|&gt;1 &nbsp;
            &#128309; BST normal &nbsp;
            &#128992; AVL balanceado &nbsp;
            <code>fb</code> = factor de balance &nbsp; <code>pts</code> = puntaje &nbsp; <code>h</code> = altura
          </div>
        </div>
      </div>
      <div class="row g-3">
        <div class="col-lg-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between">
              <span class="fw-bold" style="color:#58a6ff">&#127794; BST — Árbol Binario de Búsqueda</span>
              <span class="badge bg-primary" id="bstBadge">vacío</span>
            </div>
            <div class="card-body tree-wrap p-1" id="bstWrap" style="min-height:300px"></div>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between">
              <span class="fw-bold" style="color:#f0883e">&#9878;&#65039; AVL — Auto-balanceado</span>
              <span class="badge" style="background:#f0883e" id="avlBadge">vacío</span>
            </div>
            <div class="card-body tree-wrap p-1" id="avlWrap" style="min-height:300px"></div>
          </div>
        </div>
      </div>
    </div>

  </div><!-- /tab-content -->
</div>

<!-- MODAL LOGIN ADMIN -->
<div class="modal fade" id="adminLoginModal" tabindex="-1" data-bs-backdrop="static">
  <div class="modal-dialog modal-sm modal-dialog-centered">
    <div class="modal-content" style="background:#161b22;border:1px solid #30363d">
      <div class="modal-header border-secondary">
        <h5 class="modal-title">&#128274; Acceso Admin</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <label class="form-label text-muted small">Contraseña de administrador</label>
        <input type="password" id="adminLoginPass" class="form-control mb-3" placeholder="Ingresá la clave"
               onkeydown="if(event.key==='Enter')adminLogin()">
        <button type="button" class="btn btn-danger w-100" onclick="adminLogin()">Ingresar</button>
      </div>
    </div>
  </div>
</div>

<!-- MODAL ADMIN -->
<div class="modal fade" id="adminModal" tabindex="-1">
  <div class="modal-dialog modal-xl modal-dialog-scrollable">
    <div class="modal-content" style="background:#161b22;border:1px solid #30363d">
      <div class="modal-header border-secondary">
        <h5 class="modal-title">&#128274; Panel Administrador</h5>
        <div class="d-flex gap-2 align-items-center">
          <button type="button" class="btn btn-outline-warning btn-sm" onclick="adminDatosAlAzar()">&#127922; Datos al azar</button>
          <button type="button" class="btn btn-outline-danger btn-sm" onclick="adminReiniciar()">&#128465; Reiniciar juego</button>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
        </div>
      </div>
      <div class="modal-body">
        <ul class="nav nav-tabs mb-3">
          <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#aGrupos">Resultados Grupos</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#aBracket">Bracket / Partidos</a></li>
        </ul>
        <div class="tab-content">
          <div class="tab-pane fade show active" id="aGrupos">
            <div class="row g-2" id="adminGrForm"></div>
          </div>
          <div class="tab-pane fade" id="aBracket">
            <div id="adminBrForm"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
let S = {}; // state
let me = null;
const localPron = {};
let adminPass = '';
const ADMIN_PASS = 'profe2026';

// ─── LOAD ───────────────────────────────────────────
async function load() {
  const r = await fetch('/api/state');
  S = await r.json();
  renderGrupos();
  renderBracket();
  renderRanking();
}

// ─── GRUPOS ─────────────────────────────────────────
function renderGrupos() {
  const g = document.getElementById('gruposGrid');
  if (!g) return;
  g.innerHTML = Object.entries(S.grupos || {}).map(([k, eqs]) => {
    const res = S.resGrupos?.[k];
    return `<div class="col-sm-6 col-xl-4 col-xxl-3">
      <div class="card grupo-card h-100">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span class="badge-g">GRUPO ${k}</span>
          ${res ? '<span class="badge bg-success" style="font-size:.65rem">Finalizado</span>'
                : '<span class="badge bg-secondary" style="font-size:.65rem">En juego</span>'}
        </div>
        <div class="card-body py-2">
          ${eqs.map(eq => {
            let cls = '';
            if (res) { if (eq===res[0]) cls='prim'; else if (eq===res[1]) cls='seg'; }
            return `<div class="equipo-item ${cls}">${eq}
              ${res&&eq===res[0]?' <span class="badge bg-success float-end">1°</span>':''}
              ${res&&eq===res[1]?' <span class="badge bg-warning text-dark float-end">2°</span>':''}
            </div>`;
          }).join('')}
        </div>
      </div></div>`;
  }).join('');
}

// ─── BRACKET ────────────────────────────────────────
function renderBracket() {
  const c = document.getElementById('bracketWrap');
  if (!c) return;
  const fases = ['OCTAVOS','CUARTOS','SEMIS','FINAL'];
  let html = '<div class="d-flex gap-4">';
  for (const fase of fases) {
    const ms = (S.partidos||[]).filter(p => p.fase===fase);
    html += `<div style="min-width:170px">
      <h6 class="text-muted text-center fw-bold mb-3">${fase}</h6>
      <div class="d-flex flex-column gap-2">`;
    for (const m of ms) {
      const w1=m.res==='EQ1', w2=m.res==='EQ2';
      html += `<div class="bm">
        <div class="t ${w1?'win':''}">${m.eq1}</div>
        <div class="text-center text-muted" style="font-size:.65rem;line-height:1">vs</div>
        <div class="t ${w2?'win':''}">${m.eq2}</div>
      </div>`;
    }
    html += '</div></div>';
  }
  c.innerHTML = html + '</div>';
}

// ─── RANKING ────────────────────────────────────────
function renderRanking() {
  const tb = document.getElementById('rankingTbl');
  if (!tb) return;
  const r = S.ranking || [];
  tb.innerHTML = r.map((x,i) =>
    `<tr><td class="text-muted">${i===0?'&#129351;':i===1?'&#129352;':i===2?'&#129353;':i+1}</td>
     <td>${x.nombre}</td>
     <td class="text-end fw-bold" style="color:#58a6ff">${x.puntaje}</td></tr>`
  ).join('');
  const sr = document.getElementById('statsRow');
  if (sr) sr.innerHTML = [
    [r.length, 'Jugadores'],
    [r[0]?.puntaje ?? 0, 'Puntaje líder'],
    [(S.partidos||[]).filter(p=>p.res).length, 'Partidos jugados']
  ].map(([n,l]) => `<div class="col-4"><div class="stat-box"><div class="stat-num">${n}</div><div class="text-muted small">${l}</div></div></div>`).join('');
}

// ─── PRONÓSTICOS ────────────────────────────────────
async function inscribir() {
  const n = document.getElementById('inputNombre').value.trim();
  if (!n) return alert('Ingresá tu nombre');
  const r = await api('/api/inscribir', {nombre: n});
  if (r.error) return alert(r.error);
  me = r.nombre;
  await load();
  document.getElementById('userLabel').textContent = me;
  buildPronGrupos();
  show('s1');
}

function buildPronGrupos() {
  const f = document.getElementById('pronGruposForm');
  f.innerHTML = Object.entries(S.grupos || {}).map(([k, eqs]) => {
    const opts = eqs.map(e => `<option value="${e}">${e}</option>`).join('');
    return `<div class="col-md-6 col-xl-4">
      <div class="card p-3">
        <div class="badge-g mb-2">GRUPO ${k}</div>
        <label class="text-muted small">1° lugar</label>
        <select class="form-select form-select-sm mb-2" id="pg_${k}_1"><option value="">-- Elegir --</option>${opts}</select>
        <label class="text-muted small">2° lugar</label>
        <select class="form-select form-select-sm" id="pg_${k}_2"><option value="">-- Elegir --</option>${opts}</select>
      </div></div>`;
  }).join('');
}

async function goPartidos() {
  for (const k of Object.keys(S.grupos || {})) {
    const v1 = document.getElementById(`pg_${k}_1`)?.value;
    const v2 = document.getElementById(`pg_${k}_2`)?.value;
    if (v1 && v2 && v1 !== v2)
      await api('/api/pron_grupo', {nombre: me, grupo: k, primero: v1, segundo: v2});
  }
  await load();
  buildPronPartidos();
  show('s2');
}

function buildPronPartidos() {
  const f = document.getElementById('pronPartidosForm');
  const ms = (S.partidos||[]).filter(p => p.fase!=='TERCER_PUESTO' && p.eq1!=='TBD' && !p.res);
  if (!ms.length) {
    f.innerHTML = '<div class="text-muted p-3">Los partidos de eliminatoria se habilitan cuando el profe carga los resultados de grupos.</div>';
    return;
  }
  const fases = [...new Set(ms.map(p => p.fase))];
  f.innerHTML = fases.map(fase => {
    const rows = ms.filter(p => p.fase===fase).map(p =>
      `<div class="col-md-6"><div class="card p-3">
        <div class="text-muted small mb-1">${p.fase} &mdash; Partido ${p.id}</div>
        <div class="d-flex gap-2 align-items-center">
          <button id="pb_${p.id}_EQ1" class="btn btn-outline-secondary btn-sm flex-fill" onclick="selP(${p.id},'EQ1')">${p.eq1}</button>
          <span class="text-muted small">vs</span>
          <button id="pb_${p.id}_EQ2" class="btn btn-outline-secondary btn-sm flex-fill" onclick="selP(${p.id},'EQ2')">${p.eq2}</button>
        </div>
      </div></div>`
    ).join('');
    return `<h6 class="text-muted mt-3">${fase}</h6><div class="row g-2">${rows}</div>`;
  }).join('');
}

function selP(id, eq) {
  localPron[id] = eq;
  ['EQ1','EQ2'].forEach(e => {
    const b = document.getElementById(`pb_${id}_${e}`);
    if (b) b.className = `btn btn-sm flex-fill ${e===eq?'btn-primary':'btn-outline-secondary'}`;
  });
}

async function savePartidos() {
  for (const [id, pred] of Object.entries(localPron))
    await api('/api/pron_partido', {nombre: me, id: parseInt(id), pred});
  await load();
  show('s3');
}

function llenarGruposAlAzar() {
  for (const [k, eqs] of Object.entries(S.grupos || {})) {
    const shuffled = [...eqs].sort(() => Math.random() - 0.5);
    const s1 = document.getElementById(`pg_${k}_1`);
    const s2 = document.getElementById(`pg_${k}_2`);
    if (s1) s1.value = shuffled[0];
    if (s2) s2.value = shuffled[1];
  }
}

function llenarPartidosAlAzar() {
  const ms = (S.partidos || []).filter(p => p.fase !== 'TERCER_PUESTO' && p.eq1 !== 'TBD' && !p.res);
  for (const p of ms) selP(p.id, Math.random() < 0.5 ? 'EQ1' : 'EQ2');
}

async function datosAlAzar() {
  if (!me) {
    const nombre = 'Jugador_' + Math.floor(1000 + Math.random() * 9000);
    document.getElementById('inputNombre').value = nombre;
    const r = await api('/api/inscribir', {nombre});
    if (r.error) return alert(r.error);
    me = r.nombre;
    await load();
    document.getElementById('userLabel').textContent = me;
    buildPronGrupos();
    show('s1');
  }
  const step = ['s0','s1','s2','s3'].find(s => document.getElementById(s)?.classList.contains('on'));
  if (step === 's0' || step === 's1') {
    if (!document.getElementById('pg_A_1')) { buildPronGrupos(); }
    llenarGruposAlAzar();
    await goPartidos();
  }
  if (step === 's2' || step === 's1') {
    llenarPartidosAlAzar();
    const hay = Object.keys(localPron).length > 0;
    if (hay) await savePartidos();
    else if (step === 's1') show('s2');
  }
}

// ─── ÁRBOLES ────────────────────────────────────────
async function loadTrees() {
  const r = await fetch('/api/trees');
  const d = await r.json();
  document.getElementById('hBST').textContent = d.alturaBST;
  document.getElementById('hAVL').textContent = d.alturaAVL;
  document.getElementById('bstBadge').textContent = d.bst ? `h=${d.alturaBST}` : 'vacío';
  document.getElementById('avlBadge').textContent = d.avl ? `h=${d.alturaAVL}` : 'vacío';
  draw('bstWrap', d.bst, '#58a6ff', 'bst');
  draw('avlWrap', d.avl, '#f0883e', 'avl');
  draw('avlPtsWrap', d.avlPuntajes, '#3fb950', 'puntajes');
}

function draw(id, data, color, mode) {
  const el = document.getElementById(id);
  el.innerHTML = '';
  if (!data || data === 'null' || typeof data !== 'object') {
    el.innerHTML = '<p class="text-muted text-center pt-4" style="opacity:.5">Árbol vacío</p>'; return;
  }
  const W = Math.max(el.clientWidth || 500, 380);
  const root = d3.hierarchy(data);
  const depth = root.height;
  const H = Math.max(depth * 110 + 80, 280);
  const tree = d3.tree().size([W - 40, H - 60]);
  tree(root);
  const nodes = root.descendants();
  const maxPts = mode === 'puntajes'
    ? Math.max(0, ...nodes.map(d => d.data.pts ?? 0)) : 0;
  const svg = d3.select('#'+id).append('svg').attr('width', W).attr('height', H);
  const g = svg.append('g').attr('transform','translate(20,50)');
  g.selectAll('.lk').data(root.links()).enter().append('path')
    .attr('fill','none').attr('stroke','#444').attr('stroke-width',1.5)
    .attr('d', d3.linkVertical().x(d=>d.x).y(d=>d.y));
  const node = g.selectAll('.nd').data(nodes).enter().append('g')
    .attr('transform', d=>`translate(${d.x},${d.y})`);
  node.append('circle').attr('r',22)
    .attr('fill', d => {
      if (mode === 'avl' && d.data.fb !== undefined && Math.abs(d.data.fb) > 1) return '#da3633';
      if (mode === 'puntajes' && maxPts > 0 && (d.data.pts ?? 0) === maxPts) return '#f0883e';
      return color;
    })
    .attr('stroke','#0d1117').attr('stroke-width',2);
  node.append('text').attr('dy','.35em').attr('text-anchor','middle')
    .attr('fill','#fff').attr('font-size','10px').attr('font-weight','600')
    .text(d => { const n = d.data.name||''; return n.length>11 ? n.slice(0,10)+'…' : n; });
  if (mode === 'avl') {
    node.append('text').attr('dy','-2.1em').attr('text-anchor','middle')
      .attr('fill','#8b949e').attr('font-size','9px')
      .text(d => {
        if (d.data.fb === undefined) return '';
        let s = d.data.pts !== undefined ? `pts:${d.data.pts} ` : '';
        return s + `fb:${d.data.fb} h:${d.data.h}`;
      });
  } else if (mode === 'puntajes') {
    node.append('text').attr('dy','-2.1em').attr('text-anchor','middle')
      .attr('fill','#8b949e').attr('font-size','9px')
      .text(d => d.data.pts !== undefined ? `pts:${d.data.pts} h:${d.data.h}` : '');
  }
}

// ─── ADMIN ──────────────────────────────────────────
function openAdmin() {
  const inp = document.getElementById('adminLoginPass');
  if (inp) inp.value = '';
  new bootstrap.Modal(document.getElementById('adminLoginModal')).show();
}

function adminLogin() {
  const p = document.getElementById('adminLoginPass')?.value || '';
  if (p !== ADMIN_PASS) return alert('Contraseña incorrecta');
  adminPass = p;
  const loginEl = document.getElementById('adminLoginModal');
  const loginModal = bootstrap.Modal.getInstance(loginEl) || new bootstrap.Modal(loginEl);
  loginModal.hide();
  showAdminPanel();
}

function showAdminPanel() {
  buildAdminGrupos();
  buildAdminBracket();
  new bootstrap.Modal(document.getElementById('adminModal')).show();
}

function pass() { return adminPass; }

function buildAdminGrupos() {
  document.getElementById('adminGrForm').innerHTML = Object.entries(S.grupos||{}).map(([k, eqs]) => {
    const res = S.resGrupos?.[k] || [];
    const opts = pos => eqs.map(e=>`<option value="${e}" ${res[pos]===e?'selected':''}>${e}</option>`).join('');
    return `<div class="col-md-6"><div class="card p-3">
      <div class="badge-g mb-2">GRUPO ${k}</div>
      <div class="row g-1">
        ${[0,1,2,3].map(i=>`<div class="col-6">
          <label class="text-muted" style="font-size:.7rem">${i+1}° lugar</label>
          <select class="form-select form-select-sm" id="ag_${k}_${i}">
            <option value="">--</option>${opts(i)}</select></div>`).join('')}
      </div>
      <button class="btn btn-success btn-sm mt-2 w-100" onclick="saveGrupo('${k}')">Guardar Grupo ${k}</button>
    </div></div>`;
  }).join('');
}

function buildAdminBracket() {
  const fases = ['OCTAVOS','CUARTOS','SEMIS','TERCER_PUESTO','FINAL'];
  document.getElementById('adminBrForm').innerHTML = fases.map(fase => {
    const ms = (S.partidos||[]).filter(p=>p.fase===fase);
    return `<h6 class="text-muted mt-3">${fase}</h6><div class="row g-2">`+
      ms.map(m=>`<div class="col-md-6 col-lg-4"><div class="card p-2">
        <div class="text-muted small mb-1">Partido ${m.id}</div>
        <div class="row g-1">
          <div class="col-5"><input class="form-control form-control-sm" id="ae_${m.id}_1" value="${m.eq1}" placeholder="EQ1"></div>
          <div class="col-5"><input class="form-control form-control-sm" id="ae_${m.id}_2" value="${m.eq2}" placeholder="EQ2"></div>
          <div class="col-12 mt-1">
            <select class="form-select form-select-sm" id="ar_${m.id}">
              <option value="" ${!m.res?'selected':''}>Sin resultado</option>
              <option value="EQ1" ${m.res==='EQ1'?'selected':''}>Gana Equipo 1</option>
              <option value="EQ2" ${m.res==='EQ2'?'selected':''}>Gana Equipo 2</option>
            </select>
          </div>
          <div class="col-12"><button class="btn btn-primary btn-sm w-100 mt-1" onclick="savePtd(${m.id})">Guardar P${m.id}</button></div>
        </div>
      </div></div>`).join('')+'</div>';
  }).join('');
}

async function saveGrupo(k, quiet) {
  const vals = [0,1,2,3].map(i => document.getElementById(`ag_${k}_${i}`)?.value || '');
  if (!vals[0] || !vals[1]) { if (!quiet) alert('Ingresá al menos 1° y 2°'); return false; }
  const r = await api('/api/res_grupo', {pass:pass(), grupo:k, p1:vals[0], p2:vals[1], p3:vals[2], p4:vals[3]});
  if (r.error) { alert(r.error); return false; }
  await load(); buildAdminGrupos(); buildAdminBracket();
  if (!quiet) alert(`Grupo ${k} guardado`);
  return true;
}

async function savePtd(id, quiet) {
  const eq1 = document.getElementById(`ae_${id}_1`)?.value || 'TBD';
  const eq2 = document.getElementById(`ae_${id}_2`)?.value || 'TBD';
  const res  = document.getElementById(`ar_${id}`)?.value || '';
  const r1 = await api('/api/set_partido', {pass:pass(), id, eq1, eq2});
  if (r1.error) { alert(r1.error); return false; }
  if (res) {
    const r2 = await api('/api/res_partido', {pass:pass(), id, res});
    if (r2.error) { alert(r2.error); return false; }
  }
  await load(); buildAdminBracket(); buildAdminGrupos();
  return true;
}

function adminLlenarGruposAlAzar() {
  for (const [k, eqs] of Object.entries(S.grupos || {})) {
    const shuffled = [...eqs].sort(() => Math.random() - 0.5);
    for (let i = 0; i < 4; i++) {
      const el = document.getElementById(`ag_${k}_${i}`);
      if (el) el.value = shuffled[i] || '';
    }
  }
}

function adminPick2Teams(pool) {
  const shuffled = [...pool].sort(() => Math.random() - 0.5);
  return [shuffled[0], shuffled[1] || shuffled[0]];
}

async function adminDatosAlAzar() {
  if (!adminPass) return alert('Volvé a ingresar como admin');
  adminLlenarGruposAlAzar();
  const gruposSnap = {};
  for (const k of Object.keys(S.grupos || {})) {
    const vals = [0,1,2,3].map(i => document.getElementById(`ag_${k}_${i}`)?.value || '');
    if (!vals[0] || !vals[1]) return alert('Error al generar grupo ' + k);
    gruposSnap[k] = vals;
  }
  for (const [k, vals] of Object.entries(gruposSnap)) {
    const r = await api('/api/res_grupo', {pass:pass(), grupo:k, p1:vals[0], p2:vals[1], p3:vals[2], p4:vals[3]});
    if (r.error) return alert(r.error);
  }
  await load();
  buildAdminGrupos();
  buildAdminBracket();
  const pool = [...new Set(Object.values(S.grupos || {}).flat())];
  if (!pool.length) return alert('No hay equipos cargados');
  const partidosSnap = (S.partidos || []).map(p => {
    let eq1 = p.eq1, eq2 = p.eq2;
    if (eq1 === 'TBD' || eq2 === 'TBD') [eq1, eq2] = adminPick2Teams(pool);
    return {id: p.id, eq1, eq2, res: Math.random() < 0.5 ? 'EQ1' : 'EQ2'};
  });
  for (const pt of partidosSnap) {
    const r1 = await api('/api/set_partido', {pass:pass(), id: pt.id, eq1: pt.eq1, eq2: pt.eq2});
    if (r1.error) return alert(r1.error);
    const r2 = await api('/api/res_partido', {pass:pass(), id: pt.id, res: pt.res});
    if (r2.error) return alert(r2.error);
  }
  await load();
  buildAdminGrupos();
  buildAdminBracket();
  alert('Resultados al azar guardados (grupos y partidos)');
}

async function adminReiniciar() {
  if (!adminPass) return alert('Volvé a ingresar como admin');
  if (!confirm('¿Reiniciar el juego? Se borrarán estudiantes, pronósticos, resultados de grupos y del bracket.')) return;
  const r = await api('/api/reiniciar', {pass: pass()});
  if (r.error) return alert(r.error);
  me = null;
  for (const k of Object.keys(localPron)) delete localPron[k];
  await load();
  buildAdminGrupos();
  buildAdminBracket();
  alert('Juego reiniciado');
}

// ─── UTILS ──────────────────────────────────────────
async function api(url, body) {
  const r = await fetch(url, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body)});
  return r.json();
}

function show(id) {
  ['s0','s1','s2','s3'].forEach(s => {
    const el = document.getElementById(s);
    if (el) el.classList.toggle('on', s===id);
  });
}

document.getElementById('tabArbolesLink').addEventListener('click', () => setTimeout(loadTrees, 100));
document.querySelectorAll('[href="#tRanking"]').forEach(el => el.addEventListener('click', () => setTimeout(loadTrees, 100)));

load();
setInterval(load, 30000);
</script>
</body>
</html>
""";
}
