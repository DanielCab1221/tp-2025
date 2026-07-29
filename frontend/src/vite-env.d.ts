/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_USER_SVC_URL: string;
  readonly VITE_GESTION_SVC_URL: string;
  readonly VITE_RESERVAS_SVC_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
