export function extractErrorMessage(err: unknown): string {
  const anyErr = err as any;
  return anyErr?.response?.data?.message || anyErr?.message || 'Something went wrong';
}
