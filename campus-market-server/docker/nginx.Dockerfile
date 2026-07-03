FROM nginx:1.27-alpine
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=web-dist . /usr/share/nginx/html/web
COPY --from=admin-dist . /usr/share/nginx/html/admin

EXPOSE 80
