package org.example.mirimilibe.post.dto;

import java.util.List;

public class RecentDtos {
	public record RecentReq(String q) {}
	public record RecentRes(List<String> keywords) {}
}