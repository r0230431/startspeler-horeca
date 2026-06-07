#!/bin/bash
set -e

echo "=== 1. Mappen aanmaken ==="
sudo mkdir -p /opt/horeca
sudo mkdir -p /var/www/startspeler

echo "=== 2. Backend env aanmaken ==="
sudo tee /etc/horeca.env > /dev/null << 'EOF'
DB_HOST=ID417590_Horeca.db.webhosting.be
DB_URL=jdbc:mysql://ID417590_Horeca.db.webhosting.be:3306/ID417590_Horeca?useSSL=false&allowPublicKeyRetrieval=true&preserveInstants=false
DB_NAME=ID417590_Horeca
DB_USER=ID417590_Horeca
DB_PASSWORD=DB_StartSpeler2026!
JWT_SECRET=Ym56Fjwho8WRaItHabvy
JWT_ISSUER=startspeler
JWT_AUDIENCE=startspeler_users
JWT_TOKEN_ACCESS_HOURS=12
JWT_TOKEN_REFRESH_DAYS=30
KTOR_PORT=8081
EOF
sudo chmod 600 /etc/horeca.env

echo "=== 3. Oude Docker containers stoppen ==="
sudo docker stop startspeler-backend startspeler-db 2>/dev/null || true

echo "=== 4. Systemd service installeren ==="
sudo cp /home/administrator/horeca-backend.service /etc/systemd/system/horeca-backend.service
sudo systemctl daemon-reload
sudo systemctl enable horeca-backend
sudo systemctl restart horeca-backend

echo "=== 5. Nginx configureren ==="
sudo cp /home/administrator/nginx-startspeler /etc/nginx/sites-available/startspeler
sudo ln -sf /etc/nginx/sites-available/startspeler /etc/nginx/sites-enabled/startspeler
sudo nginx -t && sudo systemctl reload nginx

echo "=== 6. SSL certificaat aanvragen ==="
sudo certbot --nginx -d startspeler.gregoryverlinden.be --non-interactive --agree-tos -m admin@gregoryverlinden.be

echo "=== Klaar! ==="
sudo systemctl status horeca-backend --no-pager
