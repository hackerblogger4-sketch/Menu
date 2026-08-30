FROM node:22-alpine
WORKDIR /app

COPY backend/package*.json ./backend/
WORKDIR /app/backend
RUN npm install --omit=dev

WORKDIR /app
COPY backend ./backend
COPY web-menu ./web-menu
WORKDIR /app/backend
RUN mkdir -p /var/data

ENV NODE_ENV=production
ENV DB_FILE=/var/data/lagan.db
EXPOSE 10000
CMD ["npm", "start"]
