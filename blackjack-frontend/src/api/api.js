const BASE_URL = 'http://localhost:8081/api/game';

// Hàm helper để xử lý các request
async function request(endpoint, options = {}) {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        credentials: 'include' // Gửi cookie session
    });
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
}

export const getGameState = () => request('/state');

export const placeBet = (amount) => {
  const params = new URLSearchParams({ amount });
  return request(`/bet?${params}`, { method: 'POST' });
};

export const resolveInsurance = (buy) => {
  const params = new URLSearchParams({ buy });
  return request(`/insurance?${params}`, { method: 'POST' });
};

// Các hàm hành động cần handIndex
export const hit = (handIndex) => {
    const params = new URLSearchParams({ handIndex });
    return request(`/hit?${params}`, { method: 'POST' });
};

export const stand = (handIndex) => {
    const params = new URLSearchParams({ handIndex });
    return request(`/stand?${params}`, { method: 'POST' });
};

export const doubleDown = (handIndex) => {
    const params = new URLSearchParams({ handIndex });
    return request(`/double?${params}`, { method: 'POST' });
};

export const split = (handIndex) => {
    const params = new URLSearchParams({ handIndex });
    return request(`/split?${params}`, { method: 'POST' });
};