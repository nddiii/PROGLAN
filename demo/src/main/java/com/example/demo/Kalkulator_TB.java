package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Kalkulator_TB extends Application {

    private TextField display;
    private StringBuilder input = new StringBuilder();
    private boolean expectOperand = true;

    @Override
    public void start(Stage stage) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        display = new TextField();
        display.setEditable(false);
        gridPane.add(display, 0, 0, 4, 1);

        String[][] buttonLabels = {
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "=", "+"},
                {"C", "←", "(", ")", "-"}
        };

        for (int row = 0; row < buttonLabels.length; row++) {
            for (int col = 0; col < buttonLabels[row].length; col++) {
                Button button = new Button(buttonLabels[row][col]);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction(event -> handleButtonClick(buttonLabels[finalRow][finalCol]));

                // Penyesuaian gaya (CSS) untuk memperbesar tombol
                button.setStyle("-fx-font-size: 16");

                gridPane.add(button, col, row + 1);
            }
        }

        Scene scene = new Scene(gridPane, 320, 400);
        stage.setTitle("Calculator Mini");
        stage.setScene(scene);
        stage.show();
    }

    private void handleButtonClick(String label) {
        if ("=".equals(label)) {
            calculateResult();
        } else if ("C".equals(label)) {
            clearDisplay();
        } else if ("←".equals(label)) {
            removeLastCharacter();
        } else {
            input.append(label);
            display.setText(input.toString());
        }
    }

    private void calculateResult() {
        try {
            if (input.length() > 0) {
                String result = evaluateExpression(input.toString());
                display.setText(result);
                input.setLength(0);
            }
        } catch (Exception e) {
            display.setText("Error");
            input.setLength(0);
            e.printStackTrace();
        }
    }

    private String evaluateExpression(String expression) {
        Deque<String> postfixQueue = infixToPostfix(expression);
        Deque<Double> operandStack = new ArrayDeque<>();

        while (!postfixQueue.isEmpty()) {
            String token = postfixQueue.poll();

            if (isNumber(token)) {
                operandStack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if ("-".equals(token) && (operandStack.isEmpty() || !isNumber(postfixQueue.peek()))) {
                    // Handle unary negatif
                    operandStack.push(-operandStack.pop());
                } else {
                    double operand2 = operandStack.pop();
                    double operand1 = operandStack.pop();

                    switch (token) {
                        case "+":
                            operandStack.push(operand1 + operand2);
                            break;
                        case "-":
                            operandStack.push(operand1 - operand2);
                            break;
                        case "*":
                            operandStack.push(operand1 * operand2);
                            break;
                        case "/":
                            if (operand2 == 0) {
                                throw new ArithmeticException("Division by zero");
                            }
                            operandStack.push(operand1 / operand2);
                            break;
                        default:
                            throw new RuntimeException("Invalid operator");
                    }
                }
            }
        }

        if (operandStack.size() != 1) {
            throw new RuntimeException("Invalid expression");
        }

        return String.valueOf(operandStack.pop());
    }

    private Deque<String> infixToPostfix(String infixExpression) {
        Deque<String> postfixQueue = new ArrayDeque<>();
        Deque<String> operatorStack = new ArrayDeque<>();

        Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*|[+\\-*/=()]");
        Matcher matcher = pattern.matcher(infixExpression);

        while (matcher.find()) {
            String token = matcher.group();

            if (isNumber(token)) {
                postfixQueue.offer(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && precedence(token) <= precedence(operatorStack.peek())) {
                    postfixQueue.offer(operatorStack.pop());
                }
                operatorStack.push(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            postfixQueue.offer(operatorStack.pop());
        }

        return postfixQueue;
    }

    private boolean isNumber(String token) {
        return token.matches("-?\\d+\\.?\\d*");
    }

    private boolean isOperator(String token) {
        return "+-*/=".contains(token);
    }

    private int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }

    private void clearDisplay() {
        display.clear();
        input.setLength(0);
    }

    private void removeLastCharacter() {
        if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
            display.setText(input.toString());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
