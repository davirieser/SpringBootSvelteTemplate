import { writable } from "svelte/store";

const Store = localStorage.getItem("jwt");
export const jwt = writable<{ token?: string, username?: string, expired?: boolean} | null>(JSON.parse(Store) || null);
jwt.subscribe((value: {token?: string, username?: string, expired?: boolean} | null) => {
    localStorage.setItem("jwt", JSON.stringify(value));
});