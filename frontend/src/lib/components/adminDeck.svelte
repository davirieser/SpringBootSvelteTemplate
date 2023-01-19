<script lang="ts">
    import { addToastByRes } from '$utils/addToToastStore';
    import { fetching } from '$utils/fetching';

    export let deck; 
    let { deckId, name, description, published, blocked, cards} = deck;

    $: getCardsOfDeck();
    
    let hover = false
    function handleMouseOut() {
        hover = false
    }
    function handleMouseOver() {
        hover = true
    }
    
    async function handleBlockDeck(){
        blocked = !blocked;
        if(blocked){
            let res = await fetching("/api/block-deck", "POST", [{name:"deckId", value: deckId}]);
            addToastByRes(res);
        }
        if(!blocked){
            let res = await fetching("/api/unblock-deck", "POST", [{name:"deckId", value: deckId}]);
            addToastByRes(res);
        }
    }

    async function getCardsOfDeck(){
        let res = await fetching("/api/get-cards-of-deck", "GET", [{name: "deckId", value: deckId}]);
        cards = res.items;
    }
</script>


<!-- svelte-ignore a11y-mouse-events-have-key-events -->
{#if !blocked}
<div class="bg-slate-900 rounded-xl shadow-xl p-5 h-96 relative" on:mouseover={handleMouseOver} on:mouseout={handleMouseOut}>
        <div class="{hover ? "hidden" : "block"}" >
            <h1 class="underline flex justify-center text-xl">{name}</h1>
            <br class="my-4"/>
            <p>{description}</p>
            <br class="my-4"/>
            <div class="bottom-0 absolute mb-4">
                <div class="grid grid-rows-3 gap-2">
                    {#if cards}
                        <div class="badge badge-primary">Number of cards: {cards.length} </div>
                    {:else}
                        <div class="badge badge-error">No cards</div>
                    {/if}
                    {#if published}
                    <div class="badge badge-info">Published</div>
                    {:else}
                    <div class="badge badge-error">Not Published</div>
                    {/if}
                </div>
            </div>
        </div>

        <div class="{hover ? "block" : "hidden"} grid grid-row gap-2">
                <button class="btn btn-primary" on:click={handleBlockDeck}>Block Deck</button>
        </div>
</div>
{/if}

{#if blocked}
<!-- svelte-ignore a11y-mouse-events-have-key-events -->
<div class="bg-slate-900 rounded-xl shadow-xl p-5 h-96 relative opacity-50" on:mouseover={handleMouseOver} on:mouseout={handleMouseOut}>
    <div class="{hover ? "hidden" : "block"}" >
        <h1 class="underline flex justify-center text-xl">{name}</h1>
        <br class="my-4"/>
        <p>{description}</p>
    </div>

    <div class="{hover ? "block" : "hidden"} grid grid-row gap-2">
        <button class="btn btn-primary" on:click={handleBlockDeck}>Unblock Deck</button>
    </div>
</div>
{/if}