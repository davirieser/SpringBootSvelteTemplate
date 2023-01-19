import { writable } from "svelte/store";

const Store = localStorage.getItem("personId");
export const personIdStore = writable(Store || "");
personIdStore.subscribe(value => {
    localStorage.setItem("personId", value);
});
