export interface OwnerData {
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  telephone: string;
}

export interface PetData {
  name: string;
  birthDate: string;
  type: string;
}

let counter = 0;

/** Letters-only unique suffix: never collides across parallel scenarios and keeps names valid. */
const alpha = (): string => {
  const raw = `${Date.now()}${counter++}${Math.floor(Math.random() * 1000)}`;
  return raw.replace(/\d/g, (d) => 'abcdefghij'.charAt(Number(d)));
};

export const buildOwner = (overrides: Partial<OwnerData> = {}): OwnerData => ({
  firstName: 'Auto',
  lastName: `Tester${alpha()}`,
  address: '1 Test Street',
  city: 'Testville',
  telephone: '5551234567',
  ...overrides,
});

export const buildPet = (overrides: Partial<PetData> = {}): PetData => ({
  name: `Pet${alpha()}`,
  birthDate: '2020-01-15',
  type: 'dog',
  ...overrides,
});

/** A date `days` ahead in local time (visits must be booked for a future date). */
export const futureDate = (days = 2): string => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
