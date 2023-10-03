package com.example.chess.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveDto {
    private String from;
    private String to;
    private String promotion;

    public MoveDto(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "MoveDto{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", promotion='" + promotion + '\'' +
                '}';
    }
}
