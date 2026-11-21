export interface UserDTO {
  id: number;
  tenantId: number;
  role: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  statut: string;
}

export type UtilisateurDTO = UserDTO;

export interface CreateUtilisateurRequest {
  role: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: UserDTO;
}
