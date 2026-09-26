/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "media",
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef1ff", 100: "#e0e4ff", 200: "#c7ccff", 300: "#a3a9ff",
          400: "#7c7ffe", 500: "#5a5bfb", 600: "#3d3ff0", 700: "#3230d4",
          800: "#2a2aab", 900: "#262a87",
        },
        surface: { 50: "#fafafc", 100: "#f3f4f8", 800: "#1b1b22", 900: "#121218" },
      },
      fontFamily: { sans: ["Inter", "system-ui", "sans-serif"] },
      boxShadow: { card: "0 1px 2px rgba(16,24,40,.04), 0 1px 3px rgba(16,24,40,.06)" },
    },
  },
  plugins: [],
};
