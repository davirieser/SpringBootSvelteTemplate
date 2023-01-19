import "../app.postcss";
import App from "./Admin.svelte";

// import '@brainandbones/skeleton/themes/theme-skeleton.css';

const app = new App({
  target: document.getElementById("app"),
});

export default app;

