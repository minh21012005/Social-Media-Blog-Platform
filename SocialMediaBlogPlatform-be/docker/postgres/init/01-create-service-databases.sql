SELECT 'CREATE DATABASE social_blog_articles'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'social_blog_articles')\gexec

SELECT 'CREATE DATABASE social_blog_comments'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'social_blog_comments')\gexec

SELECT 'CREATE DATABASE social_blog_interactions'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'social_blog_interactions')\gexec

SELECT 'CREATE DATABASE social_blog_followers'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'social_blog_followers')\gexec

SELECT 'CREATE DATABASE social_blog_notifications'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'social_blog_notifications')\gexec
