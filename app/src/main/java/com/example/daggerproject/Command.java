package com.example.daggerproject;
import java.util.List;

interface Command {

    String key();
    Status handleInput(List<String> input);

}

enum Status {
    INVALID,
    HANDLED
}