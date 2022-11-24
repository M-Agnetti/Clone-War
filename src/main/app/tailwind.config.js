/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      padding: {
        '1/2': '10%',
      },
      keyframes: {
        anim: {
          '0%': { transform: 'scale(1);' },
          '10%': { transform: 'scale(1.2);' },
          '20%': { transform: 'scale(1.4);' },
          '30%': { transform: 'scale(1.6);' },
          '40%': { transform: 'scale(1.8);' },
          '50%': { transform: 'scale(2);' },
          '60%': { transform: 'scale(1.8);' },
          '70%': { transform: 'scale(1.6);' },
          '80%': { transform: 'scale(1.4);' },
          '90%': { transform: 'scale(1.2);' },
          '100%': { transform: 'scale(1);' },
        },
        flip: {
          '50%': { transform: 'rotateY(180deg);' },
        },
      },
      animation: {
        anim: 'anim 1s infinite',
        flip: 'flip 2s infinite',
      },
    },
  },
  plugins: [],
}
