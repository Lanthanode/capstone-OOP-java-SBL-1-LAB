package com.sbtms;

import com.sbtms.cli.CLIConsole;
import com.sbtms.db.DatabaseManager;
import com.sbtms.web.HttpServerApp;

import java.awt.Desktop;
import java.net.URI;

/**
 * Master Application Entry Point for SB-TMS Capstone Project.
 * Supports both Embedded Web Server Mode (Default) and Standalone CLI Mode.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology (RAIT), Nerul
 */
public class Main {
    public static void main(String[] args) {
        // Ensure Database Schema and Seed Data are initialized
        DatabaseManager.ensureInitialized();

        boolean isCli = false;
        int port = 8080;

        for (int i = 0; i < args.length; i++) {
            if ("--cli".equalsIgnoreCase(args[i])) {
                isCli = true;
            } else if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (isCli) {
            CLIConsole cli = new CLIConsole();
            cli.startInteractiveLoop();
        } else {
            try {
                HttpServerApp server = new HttpServerApp(port);
                server.start();

                // Open default browser if desktop is supported
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI("http://localhost:" + port + "/index.html"));
                    }
                } catch (Throwable t) {
                    // Headless or permission environment
                    System.out.println("[Main Notice]: Browser auto-open not supported. Please open http://localhost:" + port + "/ manually.");
                }

                // Keep main thread alive
                Thread.currentThread().join();
            } catch (Exception e) {
                System.err.println("[Main Fatal Error]: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
