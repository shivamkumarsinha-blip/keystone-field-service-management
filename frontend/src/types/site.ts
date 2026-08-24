export interface SiteDto {
  id: number;
  customerId: number;
  name: string;
  addressLine: string;
  city?: string;
  state?: string;
  postalCode?: string;
  active: boolean;
}
