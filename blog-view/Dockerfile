<<<<<<< HEAD
=======
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . ./
RUN npm run build
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

FROM nginx:alpine

WORKDIR /usr/share/nginx/html

<<<<<<< HEAD
COPY dist .


COPY nginx.conf /etc/nginx/conf.d/default.conf
=======
COPY --from=builder /app/dist ./
COPY nginx.conf /etc/nginx/conf.d/default.conf

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
