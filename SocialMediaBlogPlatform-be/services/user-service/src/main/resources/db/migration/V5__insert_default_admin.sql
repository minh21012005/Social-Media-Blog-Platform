-- Insert default admin user if not exists
INSERT INTO app_users (id, username, email, password_hash, display_name, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@chronicle.com',
    '$2a$10$Me.j1A6QcNxE7yBJwl79COtB5nIDJAOSwgxNhGnfiQJCG8o7EVlQu', -- bcrypt hash for password: 'admin1234'
    'Administrator',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

-- Insert role USER and ADMIN for the admin user
INSERT INTO user_roles (user_id, role)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'USER'),
    ('00000000-0000-0000-0000-000000000001', 'ADMIN')
ON CONFLICT (user_id, role) DO NOTHING;
