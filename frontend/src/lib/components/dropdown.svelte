<script lang="ts">
    import { fly } from "svelte/transition"
    let open = false;
    export let style = ""; 
    export let text = ""; 
    export let options = null;
    let active="";

    function handleActive(name){
        if(active !== name){
            active = name;
        }else active = "";
    }
</script>


<!-- svelte-ignore a11y-no-noninteractive-tabindex -->
<!-- svelte-ignore a11y-mouse-events-have-key-events -->
<div class:style on:mouseover={()=> open = true} on:mouseleave={()=>open=false}>
    <button class="btn m-1">{text}</button>
    <!-- svelte-ignore a11y-no-noninteractive-tabindex -->
    {#if open}
    <ul  in:fly={{y: -20, duration: 100}} out:fly={{y: -20, duration: 100}} tabindex="0" class="dropdown-content p-5 shadow-xl rounded-xl w-fit bg-slate-900 mb-5">
        {#each options as item}
            {#if item.action}
                <!-- svelte-ignore a11y-click-events-have-key-events -->
                {#if active === item.name}
                    <div class="flex">
                        <li class="p-2 hover:bg-gray-700 rounded-xl" on:click={()=> handleActive(item.name)}>{item.name}</li>
                        <i class="flex items-center bi bi-check2-circle"></i>
                    </div>
                {:else}
                    <li class="p-2 hover:bg-gray-700 rounded-xl" on:click={item.action} on:click={()=> handleActive(item.name)} >{item.name}</li>
                {/if}
            {/if}
        {/each}
    </ul>
    {/if}
</div>

<style>
    @import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css");
</style>