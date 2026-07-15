export function isTokenExpired(token: string | null): boolean {
  if (!token) return true;

  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    const payload = JSON.parse(jsonPayload);
    if (typeof payload.exp !== 'number') return false;

    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

export function decodeToken(token: string | null): any {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function isAdmin(): boolean {
  const localRole = localStorage.getItem('role');
  if (localRole) {
    const uRole = localRole.toUpperCase();
    return uRole === 'ADMIN' || uRole === 'SUPERADMIN';
  }

  const token = localStorage.getItem('token');
  if (!token) return false;
  try {
    const payload = decodeToken(token);
    const role = payload?.role?.toUpperCase() || payload?.Role?.toUpperCase();
    return role === 'ADMIN' || role === 'SUPERADMIN';
  } catch {
    return false;
  }
}
