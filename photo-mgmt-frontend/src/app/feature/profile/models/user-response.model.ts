import { Role } from './user-role.enum';


export interface UserResponse {
  userId: string;
  userName: string;
  email: string;
  role: Role;
  ZonedDateTime: string;
}
