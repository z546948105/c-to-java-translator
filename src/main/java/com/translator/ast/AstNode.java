package com.translator.ast;

public interface AstNode {
    <T> T accept(AstVisitor<T> visitor);
}