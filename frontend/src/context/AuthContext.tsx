import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { UserProfile, AuthResponse } from '../types';
import { getCurrentUser } from '../api/auth';
import { TOKEN_STORAGE_KEY } from '../api/client';
import { MOCK_USER } from '../mocks/mockData';

interface AuthContextType {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (authResponse: AuthResponse) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const login = useCallback((authResponse: AuthResponse) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, authResponse.accessToken);
    setToken(authResponse.accessToken);
    setUser(authResponse.user);
  }, []);

  const refreshUser = useCallback(async () => {
    if (!token) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      const userProfile = await getCurrentUser();
      setUser(userProfile);
    } catch {
      // Fallback to initial mock identity if running offline/demo
      setUser(MOCK_USER);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    refreshUser();

    // Listen for 401 unauthorized events
    const handleUnauthorized = () => {
      logout();
    };
    window.addEventListener('payflow:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('payflow:unauthorized', handleUnauthorized);
  }, [refreshUser, logout]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isLoading,
        login,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export function useAuthContext(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  return context;
}
