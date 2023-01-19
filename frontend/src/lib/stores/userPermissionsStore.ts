import { writable } from "svelte/store";

const Store = localStorage.getItem("userPermissions");
export const userPermissionsStore = writable(Store || []);
userPermissionsStore.subscribe(value => {
    localStorage.setItem("userPermissions", value);
});