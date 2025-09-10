package chess.ai;

import chess.*;               // ChessMatch, ChessPosition, ChessPiece, Color
import boardgame.Position;    // Position (linha/coluna 0-based)

public class EngineHelper {

    public static void applyEngineMove(ChessMatch match, UciEngine.Move mv) {
        ChessPosition src = toChessPosition(mv.from);
        ChessPosition dst = toChessPosition(mv.to);

        match.performChessMove(src, dst);

        if (match.getPromoted() != null) {
            match.replacePromotedPiece(mapPromotion(mv.promotion));
        }
    }

    // Converte matriz (0,0 no topo) -> notação de xadrez (a1..h8)
    private static ChessPosition toChessPosition(Position p) {
        char file = (char) ('a' + p.getColumn()); // 0->'a', 1->'b',...
        int rank  = 8 - p.getRow();               // 0 (topo) -> 8, 7 (base) -> 1
        return new ChessPosition(file, rank);     // construtor é público
    }

    // Tradução de promoção da engine: default Dama
    private static String mapPromotion(Character promo) {
        if (promo == null) return "Q";
        return switch (Character.toLowerCase(promo)) {
            case 'r' -> "R";
            case 'b' -> "B";
            case 'n' -> "N";
            case 'q' -> "Q";
            default  -> "Q";
        };
    }
}
