/**
 * Application Entry Point
 *
 * This file initializes the React application and mounts it to the DOM.
 * It imports the root App component and global styles.
 */
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";

// Mount the React application to the root element in the HTML
createRoot(document.getElementById("root")!).render(<App />);
