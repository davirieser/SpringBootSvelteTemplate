<script lang="ts">
    import { createEventDispatcher } from 'svelte';
    const dispatch = createEventDispatcher();
    import Markdown from '$components/markdown.svelte';

    import { addToastByRes } from '$utils/addToToastStore';
    import { fetching } from '$utils/fetching';

    export let deck; 
    let { deckId, name, description, published, blocked, numCards, numCardsToRepeat, numNotLearnedCards} = deck;
    
    $: getAllCardsToLearn();
    $: getCardsOfDeck();

    let hover = false
    function handleMouseOut() {
        hover = false
    }
    function handleMouseOver() {
        hover = true
    }
    
    
    let cardsToLearn = [];

    async function getAllCardsToLearn(){
		let res = await fetching("/api/get-all-cards-to-learn", "GET", [{name: "deckId", value: deckId}]);
		cardsToLearn = res.items;	
	}

    async function getCardsOfDeck(){
        let res = await fetching("/api/get-cards-of-deck", "GET", [{name: "deckId", value: deckId}]);
        cards = res.items;
    }

    function handleListCards() {
        dispatch('listCards');
    }
    
    function handleLearnDeck(){
        dispatch('learnDeck');
    }


	async function handleUnsubscribe(){
		let res = await fetching(`/api/unsubscribe-deck`, "POST", [{name: "deckId", value: deckId}]); 
		addToastByRes(res);
        dispatch("unsubscribe")
	}	
</script>


<!-- svelte-ignore a11y-mouse-events-have-key-events -->
{#if !blocked && published}
<div class="bg-slate-900 rounded-xl shadow-xl p-5 h-96 relative" on:mouseover={handleMouseOver} on:mouseout={handleMouseOut}>
        <div class="{hover ? "hidden" : "block"}" >
            <h1 class="underline flex justify-center text-xl">{name}</h1>
            <br class="my-4"/>
            <div class="max-h-full overflow-hidden break-all">
               <Markdown data={description}/>
            </div>
            <br class="my-4"/>
            <div class="bottom-0 absolute mb-4 mt-4">
                <div class="grid grid-rows gap-2">
                    <div class="gird grid-cols gap-2">
                        {#if numCards > 0}
                            <div class="badge badge-primary">Cards: {numCards} </div>
                            {:else}
                            <div class="badge badge-error">No cards</div>
                        {/if}
                        {#if numCardsToRepeat >0}
                            <div class="badge badge-primary">To repeat: {numCardsToRepeat} </div>
                        {:else}
                            <div class="badge badge-error">Nothing to repeat</div>
                        {/if}
                    </div>
                    <div class="gird grid-cols gap-2">
                        {#if numNotLearnedCards > 0}
                            <div class="badge badge-primary">To learn: {numNotLearnedCards} </div>
                        {:else}
                            <div class="badge badge-error">No cards to learn</div>
                        {/if}
                    </div>
                </div>
            </div>
        </div>


        <div class="{hover ? "block" : "hidden"} grid grid-row gap-2">
            <button class="btn btn-primary" on:click={handleLearnDeck}>Learn Deck</button>
            <button class="btn btn-primary" on:click={handleListCards}>List Cards</button>
            <button class="btn btn-primary" on:click={handleUnsubscribe}>Unsubscribe</button>
        </div>       
</div>
{/if}

{#if blocked || !published}
<div class="bg-slate-900 rounded-xl shadow-xl p-5 h-auto relative opacity-50">
    <div>
        <h1 class="underline flex justify-center text-xl">{name}</h1>
        <br class="mt-4"/>
        <div class="max-h-full overflow-hidden break-all">
            <Markdown data={description}/>
        </div>
    </div>
    <br class="mt-4"/>

    <button class="btn btn-primary" on:click={handleUnsubscribe}>Unsubscribe</button>
</div>
{/if}