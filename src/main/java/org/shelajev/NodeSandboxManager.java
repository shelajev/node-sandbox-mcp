package org.shelajev;


import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;

@Singleton
public class NodeSandboxManager {

    @Tool(description = "describe what tool does")
    ToolResponse whatMoveWouldHumanPlay(@ToolArg(description = "describe what parameter means") String fen) {


        return ToolResponse.success(new TextContent("output"));
    }
}
