package org.shelajev;


import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        sandboxContainer.start();
        sandboxes.put(sandboxContainer.getContainerId(), sandboxContainer);
        latestSandbox = sandboxContainer;
        return ToolResponse.success(new TextContent("started sandbox id: " + sandboxContainer.getContainerId()));
    }

    @Tool(description = "execute a command in the sandbox")
    ToolResponse exec(@ToolArg(description = "CLI command to execute") String[] command,
                      @ToolArg(description = "container id to execute in", required = false) String containerId) throws IOException, InterruptedException {
        GenericContainer sandbox = latestSandbox;
        if (containerId != null && !containerId.isEmpty()) {
            sandbox = sandboxes.get(containerId);
        }

        if (null == sandbox) {
            return ToolResponse.success(new TextContent("You need to initialize a sandbox first"));
        }
        Container.ExecResult execResult = sandbox.execInContainer(command);

        String stdout = execResult.getStdout();
        String stderr = execResult.getStderr();

        String result = "stdout: " + stdout + "\n\n";
        result += "stderr: " + stderr;
        return ToolResponse.success(new TextContent(result));
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
