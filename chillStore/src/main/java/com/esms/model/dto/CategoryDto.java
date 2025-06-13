package com.esms.model.dto;

public class CategoryDto {
    private Integer id;
    private String name;
    private Integer parent_id;

    public CategoryDto(Integer id, String name, Integer parent_id) {
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
    }
    public CategoryDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParent_id() {
        return parent_id;
    }

    public void setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
    }
}
