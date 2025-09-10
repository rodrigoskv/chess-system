package chess.ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import boardgame.Position;
import chess.ChessMatch;

public class UciEngine implements AutoCloseable {
    private final Process proc;
    private final BufferedWriter w;
    private final BufferedReader r;

    public UciEngine(String enginePath) throws IOException {
        proc = new ProcessBuilder(enginePath).redirectErrorStream(true).start();
        w = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        send("uci"); waitFor("uciok");
        send("isready"); waitFor("readyok");
        send("ucinewgame");
    }

    public Move bestMove(ChessMatch match, int movetimeMs) {
        String fen = Fen.fromMatch(match);
        try {
            send("position fen " + fen);
            send("go movetime " + movetimeMs);
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    String uci = line.split("\\s+")[1];   // ex.: e2e4, e7e8q
                    return Move.fromUci(uci);             // <— agora NÃO precisa do match
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro comunicando com UCI", e);
        }
        return null;
    }

    private void send(String s) throws IOException { w.write(s); w.write("\n"); w.flush(); }
    private void waitFor(String token) throws IOException {
        String line; while ((line = r.readLine()) != null) if (line.contains(token)) return;
    }

    @Override public void close() throws IOException {
        try { send("quit"); } catch (Exception ignored) {}
        proc.destroy();
    }

    // ===================== Move =====================
    public static class Move {
        public final Position from, to;
        public final Character promotion; // 'q','r','b','n' ou null

        public Move(Position from, Position to, Character promotion) {
            this.from = from; this.to = to; this.promotion = promotion;
        }

        // *** Usa ChessPosition para mapear UCI -> matriz, sem precisar de Board ***
     // dentro de UciEngine.Move
        public static Move fromUci(String uci) {
            char f1 = uci.charAt(0);      // 'a'..'h'
            int  r1 = uci.charAt(1) - '0';// '1'..'8' -> 1..8
            char f2 = uci.charAt(2);
            int  r2 = uci.charAt(3) - '0';

            Position fromPos = algebraicToPosition(f1, r1);
            Position toPos   = algebraicToPosition(f2, r2);

            Character promo = (uci.length() == 5) ? uci.charAt(4) : null; // 'q','r','b','n'
            return new Move(fromPos, toPos, promo);
        }

        private static Position algebraicToPosition(char file, int rank) {
            // Mesma fórmula do ChessPosition.toPosition():
            // linha 0 = topo (a8), coluna 0 = 'a'
            int row = 8 - rank;        // 8x8 padrão
            int col = file - 'a';
            return new Position(row, col);
        }

    }
}
