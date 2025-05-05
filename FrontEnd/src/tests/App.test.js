import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom"; 
import App from "../App";

test("renders home page", () => {
  render(<App />);
  
  
  expect(screen.getByText(/sign in/i)).toBeInTheDocument();
});
