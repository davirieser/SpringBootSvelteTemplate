import {writable} from 'svelte/store';

const Store = localStorage.getItem("adminSelectedUser");
export const adminSelectedUserStore = writable(JSON.parse(Store) || null);
adminSelectedUserStore.subscribe(value => {
    localStorage.setItem("adminSelectedUser", JSON.stringify(value));
});