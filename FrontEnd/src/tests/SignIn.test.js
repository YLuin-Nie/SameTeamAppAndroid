import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { MemoryRouter } from "react-router-dom"; 
import SignIn from "../components/pages/SignIn";

describe("SignIn Component", () => {
  test("renders Sign In form correctly", () => {
    render(
      <MemoryRouter> 
        <SignIn />
      </MemoryRouter>
    );

    expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
  });

  test("allows user to type in email and password", () => {
    render(
      <MemoryRouter>
        <SignIn />
      </MemoryRouter>
    );

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);

    fireEvent.change(emailInput, { target: { value: "Bob@yahoo.com" } });
    fireEvent.change(passwordInput, { target: { value: "Bob123" } });

    expect(emailInput.value).toBe("Bob@yahoo.com");
    expect(passwordInput.value).toBe("Bob123");
  });

  test("shows error when submitting empty fields", () => {
    render(
      <MemoryRouter>
        <SignIn />
      </MemoryRouter>
    );

    const signInButton = screen.getByRole("button", { name: /sign in/i });
    fireEvent.click(signInButton);

    expect(screen.getByText(/email and password are required/i)).toBeInTheDocument();
  });

  test("displays an error message for incorrect login credentials", () => {
    render(
      <MemoryRouter>
        <SignIn />
      </MemoryRouter>
    );

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const signInButton = screen.getByRole("button", { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: "wrong@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "wrongpassword" } });
    fireEvent.click(signInButton);

    expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument();
  });
});
