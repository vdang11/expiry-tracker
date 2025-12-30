/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: "#0b0f17",
        card: "#121a28",
        line: "#23304b",
        accent: "#6aa6ff",
        muted: "#9fb0d0",
      },
    },
  },
  plugins: [],
};
