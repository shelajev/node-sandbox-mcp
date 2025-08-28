package org.shelajev;


import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Singleton
public class NodeSandboxManager {
    private Map<String, GenericContainer> sandboxes = new HashMap<>();
    private GenericContainer latestSandbox;

    @Tool(description = "initialize a sandbox for node.js code")
    ToolResponse initSandbox() {

        GenericContainer sandboxContainer = new GenericContainer<>("mcr.microsoft.com/devcontainers/javascript-node:20")
                .withNetworkMode("none") // disable network!!
                .withWorkingDirectory("/workspace")
                .withCommand("sleep", "infinity");

        String npmCacheDir = System.getenv("NPM_CACHE_DIR");
        if (npmCacheDir != null && !npmCacheDir.isEmpty()) {
            sandboxContainer.withFileSystemBind(npmCacheDir, "/home/node/.nvm", BindMode.READ_ONLY);
            sandboxContainer.withEnv("NPM_CACHE_DIR", "/home/node/.nvm");
        }

        String filesDir = System.getenv("FILES_DIR");
        if (filesDir != null && !filesDir.isEmpty()) {
            sandboxContainer.withFileSystemBind(filesDir, filesDir, BindMode.READ_ONLY); // map to the same so path are the same
            sandboxContainer.withWorkingDirectory(filesDir);
        }

        sandboxContainer.start();
        sandboxes.put(sandboxContainer.getContainerId(), sandboxContainer);
        latestSandbox = sandboxContainer;
        return ToolResponse.success(new TextContent("started sandbox id: " + sandboxContainer.getContainerId()));
    }

    @Tool(description = "execute a command in the sandbox")
    ToolResponse exec(@ToolArg(description = "CLI command to execute") String[] command,
                      @ToolArg(description = "container id to execute in", required = false) String containerId,
                      @ToolArg(description = "run command in background", required = false) Boolean background) throws IOException, InterruptedException {
        GenericContainer sandbox = latestSandbox;
        if (containerId != null && !containerId.isEmpty()) {
            sandbox = sandboxes.get(containerId);
        }

        if (null == sandbox) {
            return ToolResponse.success(new TextContent("You need to initialize a sandbox first"));
        }

        final GenericContainer finalSandbox = sandbox;

        if (background != null && background) {
            CompletableFuture.runAsync(() -> {
                try {
                    finalSandbox.execInContainer(command);
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, ForkJoinPool.commonPool());
            return ToolResponse.success(new TextContent("Command started in background: " + String.join(" ", command)));
        } else {
            Container.ExecResult execResult = finalSandbox.execInContainer(command);

            String stdout = execResult.getStdout();
            String stderr = execResult.getStderr();

            String result = "stdout: " + stdout + "\n\n";
            result += "stderr: " + stderr;
            return ToolResponse.success(new TextContent(result));
        }
    }

    @Tool(description = "write a file in the sandbox")
    ToolResponse writeFile(@ToolArg(description = "contents of the file") String contents,
                           @ToolArg(description = "absolute path of the file to write") String filename,
                           @ToolArg(description = "container id to execute in", required = false) String containerId) {
        GenericContainer sandbox = latestSandbox;
        if (containerId != null && !containerId.isEmpty()) {
            sandbox = sandboxes.get(containerId);
        }
        if (null == sandbox) {
            return ToolResponse.success(new TextContent("You need to initialize a sandbox first"));
        }

        sandbox.copyFileToContainer(Transferable.of(contents.getBytes()), filename);

        return ToolResponse.success(new TextContent("success"));
    }
}
