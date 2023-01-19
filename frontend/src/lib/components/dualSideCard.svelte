<script lang="ts">
	import autosize from 'svelte-autosize';
  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();
  import Markdown from "$components/markdown.svelte";

  export let card;
  export let index = 0;
  export let editable = false;
  export let flippable = false;
  export let cardBg = "bg-slate-900";
  export let textBg = "bg-slate-800";
  export let title = "";

  let cardQuestionFocus = false; 
  let cardAnswerFocus = false;

  function handleDeleteCard(card) {
      dispatch('deleteCard', card);
  }

</script>

{#if editable}
  <div class="card p-5 w-auto {cardBg}">
    {#if title}
      <h1 class="flex justify-center text-xl">{title} {index}</h1>
    {:else}
      <h1 class="flex justify-center text-xl">Card {index}</h1>
    {/if}
    <br class="mt-4"/>
    {#if cardQuestionFocus}
    <textarea use:autosize on:mouseleave={()=>cardQuestionFocus=false} name="description" contenteditable id="divTextarea" bind:value={card.frontText} placeholder="Description" class="input bg-slate-800 min-h-[70px] h-auto w-full p-2  resize"/>
    {:else}
        <!-- svelte-ignore a11y-click-events-have-key-events -->
        <div on:click={()=>cardQuestionFocus=true} class="input bg-slate-800 min-h-[70px] h-auto w-full p-2">
            <div>
                <Markdown data={card.frontText}/>
            </div>
        </div>  
    {/if}

    <br class="mt-4"/>

    {#if cardAnswerFocus}
      <textarea use:autosize on:mouseleave={()=>cardAnswerFocus=false} name="description" contenteditable id="divTextarea" bind:value={card.backText} placeholder="Description" class="input bg-slate-800 min-h-[70px] h-auto w-full p-2  resize"/>
    {:else}
        <!-- svelte-ignore a11y-click-events-have-key-events -->
        <div on:click={()=>cardAnswerFocus=true} class="input bg-slate-800 min-h-[70px] h-auto w-full p-2">
            <div>
                <Markdown data={card.backText}/>
            </div>
        </div>  
    {/if}

    <input type="hidden" bind:value={card.frontText}/>
    <input type="hidden" bind:value={card.backText}/>
    <br class="mt-4"/>
    <div class="card-action">
      {#if flippable}
        <div class="flex justify-center">
          <span class="ml-4">Learn both sides:</span>
          <div class="flex justify-center">
            <input type="checkbox" bind:checked={card.flipped} class="ml-4 flex mx-auto justify-center items-center checkbox checkbox-primary"/>
          </div>
        </div>
      {/if}
      <br class="mt-2"/>
      <div class="flex justify-center">
        <button class="btn btn-accent" type="button" on:click={()=>handleDeleteCard(card)}>Delete Card</button>
      </div>
    </div>
  </div>
{:else}
  <div class="card p-5 w-auto {cardBg}" >
    <h1 class="flex justify-center text-xl">Card {index}</h1>


    <div class="input min-h-[70px] h-auto {textBg} p-2 w-auto ">
      <Markdown data={card.frontText}/>
    </div>  
    <br class="mt-4"/>
    <div class="input min-h-[70px] h-auto {textBg} p-2 w-auto">
      <Markdown data={card.backText}/>
    </div> 

    {#if card?.isFlipped}
      <div class="badge badge-primary">
        <span>Card is doubled to learn</span>
      </div>
    {/if}
  </div>
{/if}
