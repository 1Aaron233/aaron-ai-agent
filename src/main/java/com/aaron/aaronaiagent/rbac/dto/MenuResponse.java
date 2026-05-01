package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record MenuResponse(String name, String path, String permission, List<MenuResponse> children) {
}
