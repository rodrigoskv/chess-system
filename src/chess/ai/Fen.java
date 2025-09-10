package chess.ai;

import chess.*; // ChessMatch, ChessPiece, Color

public class Fen {

    public static String fromMatch(ChessMatch match) {
        String boardPart = boardToFen(match);
        char side = (match.getCurrentPlayer() == Color.WHITE) ? 'w' : 'b';
        // Castling e en-passant simplificados por enquanto: "- -"
        // Halfmove clock e fullmove number: "0 1" (placeholders)
        return boardPart + " " + side + " - - 0 1";
    }

 
    private static String boardToFen(ChessMatch match) {
        ChessPiece[][] pcs = match.getPieces(); // [row][col], row 0 = topo (a8)
        int rows = pcs.length;      // 8
        int cols = pcs[0].length;   // 8

        StringBuilder sb = new StringBuilder();
        for (int rank = 0; rank < rows; rank++) {        // 0..7 (TOP -> BOTTOM)
            int empty = 0;
            for (int file = 0; file < cols; file++) {    // 0..7 (a..h)
                ChessPiece p = pcs[rank][file];
                if (p == null) { empty++; continue; }
                if (empty > 0) { sb.append(empty); empty = 0; }
                sb.append(toFen(p));
            }
            if (empty > 0) sb.append(empty);
            if (rank < rows - 1) sb.append('/');
        }
        return sb.toString();
    }

    private static char toFen(ChessPiece p) {
        char ch = switch (p.getClass().getSimpleName()) {
            case "King"   -> 'k';
            case "Queen"  -> 'q';
            case "Rook"   -> 'r';
            case "Bishop" -> 'b';
            case "Knight" -> 'n';
            case "Pawn"   -> 'p';
            default -> '?';
        };
        return p.getColor() == Color.WHITE ? Character.toUpperCase(ch) : ch;
    }
}
