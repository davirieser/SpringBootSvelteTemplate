<script lang="ts">
    import Modal from "$components/Modal.svelte";
    import DualSideCard from "$components/DualSideCard.svelte";
    import Spinner from "$components/Spinner.svelte";
    import { fetching } from "$utils/fetching";

    export let listCards = false;
    export let selectedDeck = null; 

    async function getCardsFromDeck(deck){
        let res = await fetching("/api/get-cards-of-deck", "GET", [{name: "deckId", value: deck.deckId}]);
        return res.items;
    }
</script>


<Modal open={listCards} on:close={()=>listCards=false} closeOnBodyClick={true}>
    <div class="max-w-full min-w-fit">
        <h1 class="flex justify-center text-2xl font-bold">Cards of Deck {selectedDeck.name}</h1>
        <br class="mt-4"/>
        <div class="grid grid-cols-4 gap-2">
            {#await getCardsFromDeck(selectedDeck)}
                <Spinner/>
            {:then cards}
                {#each cards as card, index (card.cardId)}
                    <div>
                        <DualSideCard {card} {index} editable={false} cardBg="bg-slate-800" textBg="bg-slate-700"/>
                    </div>
                {/each}
            {/await}
        </div>
    </div>
    <br class="mt-4"/>
    <div class="flex fixed bottom-0 right-0 m-2">
        <div class="right-0">
            <button class="btn btn-primary" on:click={()=> listCards = false}>Close</button>
        </div>
    </div>
</Modal>