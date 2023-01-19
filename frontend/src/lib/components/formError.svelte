<script lang="ts">
    import { getContext } from 'svelte';
    const { errors } = getContext('form');
    export let message = null;
    export let name;
    export let key;
</script>

<div class="relative flex justify-between">
    {#if $errors?.[name]?.[key]?.error}
        <p class="text-red-500">{message || $errors[name][key].message}</p>
    {/if}
</div>
