package co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto;

public record CreateRequestDto(String title, String description, String approverUpn, String type) { }

