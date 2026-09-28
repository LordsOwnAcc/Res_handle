/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "media",
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#f0f1ff", 100: "#e3e5ff", 200: "#cbcdff", 300: "#a8abff",
          400: "#8285fd", 500: "#6062f7", 600: "#4a4aeb", 700: "#3d3bd0",
          800: "#3330a8", 900: "#2c2c85", 950: "#1a1a4d",
        },
        accent: { 400: "#2dd4bf", 500: "#14b8a6", 600: "#0d9488" },
        ink: {
          50: "#f7f7f9", 100: "#eeeef2", 200: "#d9d9e2", 300: "#b8b8c7",
          400: "#8f8fa3", 500: "#6c6c82", 600: "#54546b", 700: "#414157",
          800: "#28283a", 850: "#1e1e2c", 900: "#15151f", 950: "#0b0b12",
        },
      },
      fontFamily: { sans: ["Inter", "system-ui", "sans-serif"] },
      boxShadow: {
        xs: "0 1px 2px rgba(15,15,30,.04)",
        card: "0 1px 3px rgba(15,15,30,.06), 0 1px 2px rgba(15,15,30,.04)",
        elevated: "0 4px 16px rgba(15,15,30,.08), 0 2px 6px rgba(15,15,30,.05)",
        glow: "0 0 0 1px rgba(96,98,247,.12), 0 8px 24px rgba(96,98,247,.16)",
      },
      backgroundImage: {
        "brand-gradient": "linear-gradient(135deg, #6062f7 0%, #4a4aeb 50%, #3d3bd0 100%)",
        "mesh": "radial-gradient(at 0% 0%, rgba(96,98,247,.08) 0px, transparent 50%), radial-gradient(at 100% 0%, rgba(20,184,166,.06) 0px, transparent 50%)",
      },
      animation: {
        "fade-in": "fadeIn .35s ease-out",
        "slide-up": "slideUp .35s cubic-bezier(.16,1,.3,1)",
      },
      keyframes: {
        fadeIn: { from: { opacity: 0 }, to: { opacity: 1 } },
        slideUp: { from: { opacity: 0, transform: "translateY(8px)" }, to: { opacity: 1, transform: "translateY(0)" } },
      },
    },
  },
  plugins: [],
};
