CREATE EXTENSION pg_trgm;
CREATE INDEX title_gist ON track USING gist(title gist_trgm_ops);
