package application;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;
import chess.ai.UciEngine;
import chess.ai.EngineHelper;

public class Program {
    public static void main(String[] args) {

        ChessMatch chessMatch = new ChessMatch();
        Scanner sc = new Scanner(System.in);
        List<ChessPiece> captured = new ArrayList<>();

        // Caminho do executável do Stockfish
        String exeName = System.getProperty("os.name").toLowerCase().contains("win") ? "stockfish.exe" : "stockfish";
        String enginePath = Paths.get(System.getProperty("user.dir"), "engine", exeName).toString();

        Color iaSide = Color.BLACK; // IA joga de pretas

        try (UciEngine engine = new UciEngine(enginePath)) {
            while (!chessMatch.getCheckMate()) {
                try {
                    UI.clearScreen();
                    UI.printMatch(chessMatch, captured);
                    System.out.println();

                    if (chessMatch.getCurrentPlayer() == iaSide) {
                        // ----- turno da IA -----
                        var mv = engine.bestMove(chessMatch, 10); // 800 ms para pensar
                        if (mv == null) {
                            System.out.println("[IA] Sem lance (empate/mate).");
                            break;
                        }
                        EngineHelper.applyEngineMove(chessMatch, mv);
                        // OBS: se quiser registrar capturas da IA na lista `captured`,
                        // veja o patch do EngineHelper mais abaixo.

                    } else {
                        // ----- turno do humano -----
                        System.out.print("Source: ");
                        ChessPosition source = UI.readChessPosition(sc);

                        boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                        UI.clearScreen();
                        UI.printBoard(chessMatch.getPieces(), possibleMoves);
                        System.out.println();

                        System.out.print("Target: ");
                        ChessPosition target = UI.readChessPosition(sc);

                        ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
                        if (capturedPiece != null) {
                            captured.add(capturedPiece);
                        }

                        if (chessMatch.getPromoted() != null) {
                            System.out.print("Digite a peça para a promoção (B/N/R/Q): ");
                            String type = sc.nextLine().toUpperCase();
                            while (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
                                System.out.print("Valor inválido. Digite a peça para a promoção (B/N/R/Q): ");
                                type = sc.nextLine().toUpperCase();
                            }
                            chessMatch.replacePromotedPiece(type);
                        }
                    }

                } catch (ChessException e) {
                    System.out.println(e.getMessage());
                    sc.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("Entrada inválida");
                    sc.nextLine();
                }
            }

            UI.clearScreen();
            UI.printMatch(chessMatch, captured);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
