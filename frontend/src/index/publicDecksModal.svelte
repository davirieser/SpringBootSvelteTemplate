<script lang="ts">
    import { createEventDispatcher } from "svelte";
    let dispatch = createEventDispatcher();
    import Modal from "$components/Modal.svelte";
    import Spinner from "$components/Spinner.svelte";
    import Markdown from "$components/markdown.svelte";

    import { fetching } from "$utils/fetching";
    import { addToastByRes, addToast } from "$utils/addToToastStore";

    export let showPublicDecks = false; 
    export let selectedDeck = null; 
    export let listCards = false;
    export let selectedPublicDeck = null;

    let publicDecks = []; 
    let searchPublicDeckName = "";

    $: getPublicDecks();
    async function getPublicDecks(){
		let res = await fetching("/api/get-published-decks", "GET");
		if(res.success){
			publicDecks = res.items;
			return res.items;
		} 
		else{
			addToast("Error fetching public decks", "alert-error");
		}
	}

    async function handleSubscribe(deck){
		let res = await fetching(`/api/subscribe-deck`, "POST", [{name: "deckId", value: deck.deckId}]);
		addToastByRes(res);
        getPublicDecks();
        dispatch("refresh");
	}
</script>

<Modal open={showPublicDecks} on:close={()=>showPublicDecks=false} closeOnBodyClick={false}>
    <div class="flex flex-col min-w-fit">
        <h1 class="flex justify-center text-2xl font-bold">Public Decks</h1>
        <br class="mt-4"/>
        <input bind:value={searchPublicDeckName} placeholder="name" class="input w-full"/>
        <br class="mt-4"/>
        {#await publicDecks}
            <Spinner/>
        {:then publicDecks}
            {#if publicDecks.length == 0}
                <h1 class="text-2xl flex justify-center">No Decks Found</h1>
            {:else}
                <div class="grid grid-cols-4 gap-2">
                    {#each publicDecks as deck}
                        {#if deck.name.toLowerCase().includes(searchPublicDeckName.toLowerCase()) || deck.description.toLowerCase().includes(searchPublicDeckName.toLowerCase())} 
                            <div class="card bg-gray-700 p-5 w-fit min-w-fit">
                                <h1 class="card-title">{deck.name}</h1>
                                <p class="card-subtitle" >
                                    <Markdown data={deck.description}/>
                                </p>
                                <!-- <p class="card-subtitle">{deck.description}</p> -->
                                {#if deck.numCards > 0}
                                    <div class="badge badge-primary">Number of cards: {deck.numCards} </div>
                                    {:else}
                                    <div class="badge badge-error">No cards</div>
                                {/if}
                                <br class="pt-4"/>
                                <div class="card-actions">
                                    <button class="btn btn-primary" on:click={()=> {handleSubscribe(deck)}}>Subscribe</button>
                                    {#if deck.numCards > 0}
                                        <button class="btn btn-secondary" on:click={()=> {selectedPublicDeck=deck; listCards=true}}>Cards</button>
                                    {/if}
                                </div>
                            </div>
                        {:else}
                            <h1 class="flex justify-center text-3xl">No deck found</h1>
                        {/if}
                    {/each}
                </div>
            {/if}
        {/await}
    </div>
    <div class="mt-12 modal-action">
        <div class="flex fixed bottom-0 right-0 m-2">
            {#if selectedPublicDeck}
                <button class="btn btn-secondary m-1" on:click={()=> selectedPublicDeck=null}>Back</button>
            {/if}
            <button class="btn btn-primary m-1" on:click={()=> showPublicDecks = false}>Close</button>
        </div>
    </div>
</Modal>